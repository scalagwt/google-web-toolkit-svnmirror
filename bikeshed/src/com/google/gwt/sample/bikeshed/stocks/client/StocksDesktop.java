/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.sample.bikeshed.stocks.client;

import com.google.gwt.bikeshed.list.client.ListView;
import com.google.gwt.bikeshed.list.shared.AsyncListViewAdapter;
import com.google.gwt.bikeshed.list.shared.ListViewAdapter;
import com.google.gwt.bikeshed.list.shared.Range;
import com.google.gwt.bikeshed.tree.client.CellBrowser;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.sample.bikeshed.stocks.client.TransactionTreeViewModel.SectorListViewAdapter;
import com.google.gwt.sample.bikeshed.stocks.shared.PlayerInfo;
import com.google.gwt.sample.bikeshed.stocks.shared.StockQuote;
import com.google.gwt.sample.bikeshed.stocks.shared.StockQuoteList;
import com.google.gwt.sample.bikeshed.stocks.shared.StockRequest;
import com.google.gwt.sample.bikeshed.stocks.shared.StockResponse;
import com.google.gwt.sample.bikeshed.stocks.shared.Transaction;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desktop client for the stocks demo.
 */
public class StocksDesktop implements EntryPoint, Updater {

  interface Binder extends UiBinder<Widget, StocksDesktop> {
  }

  private static final Binder binder = GWT.create(Binder.class);

  /**
   * The delay between updates in milliseconds.
   */
  private static final int UPDATE_DELAY = 5000;

  static String getFormattedPrice(int price) {
    return NumberFormat.getCurrencyFormat("USD").format(price / 100.0);
  }

  @UiField
  Label cashLabel;

  @UiField
  FavoritesWidget favoritesWidget;
  @UiField
  Label netWorthLabel;
  @UiField
  PlayerScoresWidget playerScoresWidget;
  @UiField
  StockQueryWidget queryWidget;
  @UiField
  CellBrowser transactionTree;

  /**
   * The popup used to purchase stock.
   */
  private BuySellPopup buySellPopup = new BuySellPopup();
  private final StockServiceAsync dataService = GWT.create(StockService.class);

  private AsyncListViewAdapter<StockQuote> favoritesListViewAdapter;
  private AsyncListViewAdapter<PlayerInfo> playerScoresListViewAdapter;
  private AsyncListViewAdapter<StockQuote> searchListViewAdapter;
  private Map<String, ListViewAdapter<Transaction>> transactionListViewAdaptersByTicker = new HashMap<String, ListViewAdapter<Transaction>>();
  private ListViewAdapter<Transaction> transactionListViewAdapter;
  private List<Transaction> transactions;

  private TransactionTreeViewModel treeModel;

  /**
   * The timer used to update the stock quotes.
   */
  private Timer updateTimer = new Timer() {
    @Override
    public void run() {
      update();
    }
  };

  public void buy(StockQuote stockQuote) {
    buySellPopup.setStockQuote(stockQuote, true);
    buySellPopup.center();
  }

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    // Create the various models. Do this before binding the UI, because some
    // of the UiFactories need the models to instantiate their widgets.
    searchListViewAdapter = new AsyncListViewAdapter<StockQuote>() {
      @Override
      protected void onRangeChanged(ListView<StockQuote> view) {
        update();
      }
    };
    searchListViewAdapter.setKeyProvider(StockQuote.KEY_PROVIDER);

    favoritesListViewAdapter = new AsyncListViewAdapter<StockQuote>() {
      @Override
      protected void onRangeChanged(ListView<StockQuote> view) {
        update();
      }
    };
    favoritesListViewAdapter.setKeyProvider(StockQuote.KEY_PROVIDER);

    playerScoresListViewAdapter = new AsyncListViewAdapter<PlayerInfo>() {
      @Override
      protected void onRangeChanged(ListView<PlayerInfo> view) {
      }
    };

    treeModel = new TransactionTreeViewModel(this, favoritesListViewAdapter,
        transactionListViewAdaptersByTicker);

    transactionListViewAdapter = new ListViewAdapter<Transaction>();
    transactions = transactionListViewAdapter.getList();

    // Now create the UI.
    RootLayoutPanel.get().add(binder.createAndBindUi(this));

    // Hook up handlers to columns and the buy/sell popup.
    Columns.favoriteColumn.setFieldUpdater(new FieldUpdater<StockQuote, Boolean>() {
      public void update(int index, StockQuote object, Boolean value) {
        setFavorite(object.getTicker(), value);
      }
    });

    Columns.buyColumn.setFieldUpdater(new FieldUpdater<StockQuote, String>() {
      public void update(int index, StockQuote quote, String value) {
        buy(quote);
      }
    });

    Columns.sellColumn.setFieldUpdater(new FieldUpdater<StockQuote, String>() {
      public void update(int index, StockQuote quote, String value) {
        sell(quote);
      }
    });

    buySellPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
      public void onClose(CloseEvent<PopupPanel> event) {
        Transaction t = buySellPopup.getTransaction();
        if (t != null) {
          transact(t);
        }
      }
    });

    update();
  }

  /**
   * Process the {@link StockResponse} from the server.
   * 
   * @param response the stock response
   */
  public void processStockResponse(StockResponse response) {
    // Update the search list if the query has not changed.
    StockQuoteList searchResults = response.getSearchResults();
    String searchQuery = queryWidget.getSearchQuery();
    if (searchQuery != null && searchQuery.equals(response.getSearchQuery())) {
      searchListViewAdapter.updateDataSize(response.getNumSearchResults(), true);
      searchListViewAdapter.updateViewData(searchResults.getStartIndex(),
          searchResults.size(), searchResults);
    }

    // Update the favorites list.
    updateFavorites(response);
    updateSector(response);
    updatePlayerScores(response);

    // Update available cash.
    int cash = response.getCash();
    int netWorth = response.getNetWorth();
    cashLabel.setText(getFormattedPrice(cash));
    netWorthLabel.setText(getFormattedPrice(netWorth));
    buySellPopup.setAvailableCash(cash);

    // Restart the update timer.
    updateTimer.schedule(UPDATE_DELAY);
  }

  public void sell(StockQuote stockQuote) {
    buySellPopup.setStockQuote(stockQuote, false);
    buySellPopup.center();
  }

  /**
   * Set or unset a ticker symbol as a 'favorite'.
   * 
   * @param ticker the ticker symbol
   * @param favorite if true, make the stock a favorite
   */
  public void setFavorite(String ticker, boolean favorite) {
    if (favorite) {
      dataService.addFavorite(ticker, favoritesListViewAdapter.getRanges()[0],
          new AsyncCallback<StockResponse>() {
            public void onFailure(Throwable caught) {
              handleRpcError(caught, "Error adding favorite");
            }

            public void onSuccess(StockResponse response) {
              updateFavorites(response);
              updatePlayerScores(response);
            }
          });
    } else {
      dataService.removeFavorite(ticker,
          favoritesListViewAdapter.getRanges()[0],
          new AsyncCallback<StockResponse>() {
            public void onFailure(Throwable caught) {
              handleRpcError(caught, "Error removing favorite");
            }

            public void onSuccess(StockResponse response) {
              updateFavorites(response);
              updatePlayerScores(response);
            }
          });
    }
  }

  public void transact(Transaction t) {
    dataService.transact(t, new AsyncCallback<Transaction>() {
      public void onFailure(Throwable caught) {
        Window.alert("Error: " + caught.getMessage());
      }

      public void onSuccess(Transaction result) {
        recordTransaction(result);
        update();
      }

      /**
       * Update transactions (list of all transactions), transactionTickers (set
       * of all tickers involved in transactions), and transactionsByTicker (map
       * from ticker to lists of transactions for that ticker).
       */
      private void recordTransaction(Transaction result) {
        transactions.add(0, result);
        String ticker = result.getTicker();

        // Update the next level of the transaction tree
        // for the given ticker
        ListViewAdapter<Transaction> t = transactionListViewAdaptersByTicker.get(ticker);
        if (t == null) {
          t = new ListViewAdapter<Transaction>();
          transactionListViewAdaptersByTicker.put(ticker, t);
        }
        t.getList().add(result);
      }
    });
  }

  /**
   * Request data from the server using the last query string.
   */
  public void update() {
    if (queryWidget == null) {
      return;
    }

    updateTimer.cancel();

    Range[] searchRanges = searchListViewAdapter.getRanges();
    Range[] favoritesRanges = favoritesListViewAdapter.getRanges();

    SectorListViewAdapter sectorListViewAdapter = treeModel.getSectorListViewAdapter();
    Range[] sectorRanges = sectorListViewAdapter == null ? null
        : sectorListViewAdapter.getRanges();

    if (searchRanges == null || searchRanges.length == 0
        || favoritesRanges == null || favoritesRanges.length == 0) {
      return;
    }

    String searchQuery = queryWidget.getSearchQuery();

    StockRequest request = new StockRequest(searchQuery,
        sectorListViewAdapter != null ? sectorListViewAdapter.getSector()
            : null, searchRanges[0], favoritesRanges[0], sectorRanges != null
            && sectorRanges.length > 0 ? sectorRanges[0] : null);
    dataService.getStockQuotes(request, new AsyncCallback<StockResponse>() {
      public void onFailure(Throwable caught) {
        if (handleRpcError(caught, null)) {
          updateTimer.schedule(UPDATE_DELAY);
        }
      }

      public void onSuccess(StockResponse result) {
        processStockResponse(result);
      }
    });
  }

  @UiFactory
  FavoritesWidget createFavoritesWidget() {
    return new FavoritesWidget(favoritesListViewAdapter);
  }

  @UiFactory
  PlayerScoresWidget createPlayerScoresWidget() {
    return new PlayerScoresWidget(playerScoresListViewAdapter);
  }

  @UiFactory
  StockQueryWidget createQueryWidget() {
    return new StockQueryWidget(searchListViewAdapter, this);
  }

  @UiFactory
  CellBrowser createTransactionTree() {
    CellBrowser treeView = new CellBrowser(treeModel, null);
    treeView.setAnimationEnabled(true);
    return treeView;
  }

  /**
   * Display a message to the user when an RPC call fails.
   * 
   * @param caught the exception
   * @param displayMessage the message to display to the user, or null to
   *          display a default message
   * @return true if recoverable, false if not
   */
  private boolean handleRpcError(Throwable caught, String displayMessage) {
    String message = caught.getMessage();
    if (message.contains("Not logged in")) {
      // Force the user to login.
      Window.Location.reload();
      return false;
    }

    if (displayMessage == null) {
      displayMessage = "ERROR: " + caught.getMessage();
    }
    Window.alert(displayMessage);
    return true;
  }

  private void updateFavorites(StockResponse response) {
    // Update the favorites list.
    StockQuoteList favorites = response.getFavorites();
    favoritesListViewAdapter.updateDataSize(response.getNumFavorites(), true);
    favoritesListViewAdapter.updateViewData(favorites.getStartIndex(),
        favorites.size(), favorites);
  }

  private void updatePlayerScores(StockResponse response) {
    // Update the player scores.
    List<PlayerInfo> playerScores = response.getPlayers();
    int numPlayers = playerScores.size();
    playerScoresListViewAdapter.updateDataSize(numPlayers, true);
    playerScoresListViewAdapter.updateViewData(0, numPlayers, playerScores);
  }

  private void updateSector(StockResponse response) {
    // Update the sector list.
    StockQuoteList sectorList = response.getSector();
    if (sectorList != null) {
      SectorListViewAdapter sectorListViewAdapter = treeModel.getSectorListViewAdapter();
      if (sectorListViewAdapter != null) {
        sectorListViewAdapter.updateDataSize(response.getNumSector(), true);
        sectorListViewAdapter.updateViewData(sectorList.getStartIndex(),
            sectorList.size(), sectorList);
      }
    }
  }
}
