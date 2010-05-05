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
package com.google.gwt.sample.bikeshed.cookbook.client;

import com.google.gwt.bikeshed.list.client.CellTable;
import com.google.gwt.bikeshed.list.client.Column;
import com.google.gwt.bikeshed.list.client.Header;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionModel;
import com.google.gwt.view.client.ListViewAdapter;
import com.google.gwt.view.client.ProvidesKey;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * A recipe for mail-like selection features.
 */
public class MailRecipe extends Recipe implements ClickHandler {

  static interface GetValue<T, C> {
    C getValue(T object);
  }

  static class MailSelectionModel extends DefaultSelectionModel<Message> {
    enum Type {
      ALL(), NONE(), READ(), SENDER(), SUBJECT(), UNREAD();

      Type() {
        typeMap.put(this.toString(), this);
      }
    }

    private static ProvidesKey<Message> keyProvider = new ProvidesKey<Message>() {
      public Object getKey(Message item) {
        return Integer.valueOf(item.id);
      }
    };

    // A map from enum names to their values
    private static Map<String, Type> typeMap = new HashMap<String, Type>();

    private String search;
    private Type type = Type.NONE;

    @Override
    public ProvidesKey<Message> getKeyProvider() {
      return keyProvider;
    }

    public String getType() {
      return type.toString();
    }

    @Override
    public boolean isDefaultSelected(Message object) {
      switch (type) {
        case NONE:
          return false;
        case ALL:
          return true;
        case READ:
          return object.isRead();
        case UNREAD:
          return !object.isRead();
        case SENDER:
          if (search == null || search.length() == 0) {
            return false;
          }
          return canonicalize(object.getSender()).contains(search);
        case SUBJECT:
          if (search == null || search.length() == 0) {
            return false;
          }
          return canonicalize(object.getSubject()).contains(search);
        default:
          throw new IllegalStateException("type = " + type);
      }
    }

    public void setSearch(String search) {
      this.search = canonicalize(search);
      scheduleSelectionChangeEvent();
    }

    public void setType(String type) {
      this.type = typeMap.get(type);
      clearExceptions();
      scheduleSelectionChangeEvent();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(type.name());
      sb.append(' ');
      if (type == Type.SENDER || type == Type.SUBJECT) {
        sb.append(search);
        sb.append(' ');
      }

      // Copy the exceptions into a TreeMap in order to sort by message id
      TreeMap<Object, Boolean> exceptions = new TreeMap<Object, Boolean>();
      getExceptions(exceptions);

      appendExceptions(sb, exceptions, true);
      appendExceptions(sb, exceptions, false);

      return sb.toString();
    }

    @Override
    protected void scheduleSelectionChangeEvent() {
      selectionLabel.setText("Selected " + this.toString());
      super.scheduleSelectionChangeEvent();
    }

    private void appendExceptions(StringBuilder sb,
        Map<Object, Boolean> exceptions, boolean selected) {
      boolean first = true;
      for (Map.Entry<Object, Boolean> entry : exceptions.entrySet()) {
        if (entry.getValue() != selected) {
          continue;
        }

        if (first) {
          first = false;
          sb.append(selected ? '+' : '-');
          sb.append("msg(s) ");
        }
        sb.append(entry.getKey());
        sb.append(' ');
      }
    }

    private String canonicalize(String input) {
      return input.toUpperCase();
    }
  }

  // Hashing, comparison, and equality are based on the message id
  static class Message {
    Date date;
    int id;
    boolean isRead;
    String sender;
    String subject;

    public Message(int id, String sender, String subject, Date date) {
      super();
      this.id = id;
      this.sender = sender;
      this.subject = subject;
      this.date = date;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Message)) {
        return false;
      }
      return id == ((Message) obj).id;
    }

    public Date getDate() {
      return date;
    }

    public int getId() {
      return id;
    }

    public String getSender() {
      return sender;
    }

    public String getSubject() {
      return subject;
    }

    @Override
    public int hashCode() {
      return id;
    }

    public boolean isRead() {
      return isRead;
    }

    @Override
    public String toString() {
      return "Message [id=" + id + ", sender=" + sender + ", subject="
          + subject + ", read=" + isRead + ", date=" + date + "]";
    }
  }

  private static Label messageIdsLabel = new Label("");

  private static Label selectionLabel = new Label("Selected NONE");

  private static final String[] senders = {
      "test@example.com", "spam1@spam.com", "gwt@google.com", "Mai Oleta",
      "Barbara Myles", "Celsa Ocie", "Elwood Holloway", "Bolanle Alford",
      "Amaka Ackland", "Afia Audley", "Pearlene Cher", "Pei Sunshine",
      "Zonia Dottie", "Krystie Jetta", "Alaba Banvard", "Ines Azalee",
      "Kaan Boulier", "Emilee Naoma", "Atino Alice", "Debby Renay",
      "Versie Nereida", "Ramon Erikson", "Karole Crissy", "Nelda Olsen",
      "Mariana Dann", "Reda Cheyenne", "Edelmira Jody", "Agueda Shante",
      "Marla Dorris"};

  private static final String[] subjects = {
      "GWT rocks", "What's a widget?", "Money in Nigeria",
      "Impress your colleagues with bling-bling", "Degree available",
      "Rolex Watches", "Re: Re: yo bud", "Important notice"};

  private final Comparator<Message> dateComparator = new Comparator<Message>() {
    public int compare(Message o1, Message o2) {
      long cmp = o1.date.getTime() - o2.date.getTime();
      if (cmp < 0) {
        return -1;
      } else if (cmp > 0) {
        return 1;
      } else {
        return 0;
      }
    }
  };

  private final Comparator<Message> idComparator = new Comparator<Message>() {
    public int compare(Message o1, Message o2) {
      // Integer comparison
      return o1.id - o2.id;
    }
  };

  private Comparator<Message> lastComparator = idComparator;

  private boolean lastSortUp = true;

  private int messageId = 10000;

  private List<Message> messages;

  private MailSelectionModel selectionModel = new MailSelectionModel();

  private CellTable<Message> table;

  public MailRecipe() {
    super("Mail");
  }

  // Handle events for all buttons here in order to avoid creating multiple
  // ClickHandlers
  public void onClick(ClickEvent event) {
    String id = ((Button) event.getSource()).getElement().getId();
    if ("PAGE".equals(id)) {
      // selectionModel.setType(MailSelectionModel.NONE);
      List<Message> selectedItems = table.getDisplayedItems();
      for (Message item : selectedItems) {
        selectionModel.setSelected(item, true);
      }
    } else if (id.startsWith("ADD")) {
      addMessages(Integer.parseInt(id.substring(3)));
    } else if (id.startsWith("REM")) {
      removeMessages(Integer.parseInt(id.substring(3)));
    } else {
      selectionModel.setType(id);
    }
  }

  @Override
  protected Widget createWidget() {
    ListViewAdapter<Message> adapter = new ListViewAdapter<Message>();
    messages = adapter.getList();

    addMessages(10);

    table = new CellTable<Message>(10);
    table.setSelectionModel(selectionModel);
    adapter.addView(table);

    // The state of the checkbox is synchronized with the selection model
    SelectionColumn<Message> selectedColumn = new SelectionColumn<Message>(
        selectionModel);
    Header<Boolean> selectedHeader = new Header<Boolean>(new CheckboxCell()) {
      @Override
      public boolean dependsOnSelection() {
        return true;
      }

      @Override
      public Boolean getValue() {
        return selectionModel.getType().equals("ALL");
      }
    };
    selectedHeader.setUpdater(new ValueUpdater<Boolean>() {
      public void update(Boolean value) {
        if (value == true) {
          selectionModel.setType("ALL");
        } else if (value == false) {
          selectionModel.setType("NONE");
        }
      }
    });
    table.addColumn(selectedColumn, selectedHeader);

    addColumn(table, "ID", new TextCell(),
        new GetValue<Message, String>() {
          public String getValue(Message object) {
            return "" + object.id;
          }
        }, idComparator);

    addColumn(table, "Read", new GetValue<Message, String>() {
      public String getValue(Message object) {
        return object.isRead ? "read" : "unread";
      }
    });

    Column<Message, Date> dateColumn = addColumn(table, "Date",
        new DatePickerCell(), new GetValue<Message, Date>() {
          public Date getValue(Message object) {
            return object.date;
          }
        }, dateComparator);
    dateColumn.setFieldUpdater(new FieldUpdater<Message, Date>() {
      public void update(int index, Message object, Date value) {
        Window.alert("Changed date from " + object.date + " to " + value);
        object.date = value;
        table.refresh();
      }
    });

    addColumn(table, "Sender", new GetValue<Message, String>() {
      public String getValue(Message object) {
        return object.getSender();
      }
    });

    addColumn(table, "Subject", new GetValue<Message, String>() {
      public String getValue(Message object) {
        return object.getSubject();
      }
    });

    Column<Message, String> toggleColumn = new Column<Message, String>(
        new ButtonCell()) {
      @Override
      public String getValue(Message object) {
        return object.isRead ? "Mark Unread" : "Mark Read";
      }
    };
    toggleColumn.setFieldUpdater(new FieldUpdater<Message, String>() {
      public void update(int index, Message object, String value) {
        object.isRead = !object.isRead;
        messages.set(index, object);
      }
    });
    table.addColumn(toggleColumn, "Toggle Read/Unread");

    ScrollbarPager<Message> pager = new ScrollbarPager<Message>(table);

    Label searchLabel = new Label("Search Sender or Subject:");
    final TextBox searchBox = new TextBox();
    searchBox.addKeyUpHandler(new KeyUpHandler() {
      public void onKeyUp(KeyUpEvent event) {
        selectionModel.setSearch(searchBox.getText());
      }
    });

    HorizontalPanel panel = new HorizontalPanel();
    panel.add(searchLabel);
    panel.add(searchBox);

    FlowPanel p = new FlowPanel();
    p.add(panel);
    p.add(makeButton("Search Subject", "SUBJECT"));
    p.add(makeButton("Search Senders", "SENDER"));
    p.add(new HTML("<br>"));
    HorizontalPanel hp = new HorizontalPanel();
    hp.add(pager);
    hp.add(table);
    p.add(hp);
    p.add(new HTML("<hr>"));
    p.add(makeButton("Select None", "NONE"));
    p.add(makeButton("Select All On This Page", "PAGE"));
    p.add(makeButton("Select All", "ALL"));
    p.add(makeButton("Select Read", "READ"));
    p.add(makeButton("Select Unread", "UNREAD"));
    p.add(selectionLabel);
    p.add(new HTML("<hr>"));
    p.add(makeButton("Add 5 messages", "ADD5"));
    p.add(makeButton("Remove 5 messages", "REM5"));
    p.add(messageIdsLabel);
    return p;
  }

  private <C extends Comparable<C>> Column<Message, C> addColumn(
      CellTable<Message> table, final String text,
      final Cell<C> cell, final GetValue<Message, C> getter,
      final Comparator<Message> comparator) {
    Column<Message, C> column = new Column<Message, C>(cell) {
      @Override
      public C getValue(Message object) {
        return getter.getValue(object);
      }
    };
    Header<String> header = new Header<String>(new ClickableTextCell()) {
      @Override
      public String getValue() {
        return text;
      }
    };
    header.setUpdater(new ValueUpdater<String>() {
      boolean sortUp = true;

      public void update(String value) {
        if (comparator == null) {
          sortMessages(new Comparator<Message>() {
            public int compare(Message o1, Message o2) {
              return getter.getValue(o1).compareTo(getter.getValue(o2));
            }
          }, sortUp);
        } else {
          sortMessages(comparator, sortUp);
        }
        sortUp = !sortUp;
      }
    });
    table.addColumn(column, header);
    return column;
  }

  private Column<Message, String> addColumn(
      CellTable<Message> table, final String text,
      final GetValue<Message, String> getter) {
    return addColumn(table, text, new TextCell(), getter, null);
  }

  private void addMessages(int count) {
    Date now = new Date();
    Random rand = new Random();
    for (int i = 0; i < count; i++) {
      // Go back up to 90 days from the current date
      long dateOffset = rand.nextInt(60 * 60 * 24 * 90) * 1000L;
      Message message = new Message(messageId++,
          senders[rand.nextInt(senders.length)],
          subjects[rand.nextInt(subjects.length)], new Date(now.getTime()
              - dateOffset));
      message.isRead = rand.nextBoolean();
      messages.add(message);
    }
    sortMessages(lastComparator, lastSortUp);

    messageIdsLabel.setText("Maximum message ID = " + (messageId - 1));
  }

  private Button makeButton(String label, String id) {
    Button button = new Button(label);
    button.getElement().setId(id);
    button.addClickHandler(this);
    return button;
  }

  private void removeMessages(int count) {
    count = Math.min(count, messages.size());
    for (int i = 0; i < count; i++) {
      messages.remove(0);
    }
  }

  private void sortMessages(final Comparator<Message> comparator, boolean sortUp) {
    lastComparator = comparator;
    lastSortUp = sortUp;
    if (sortUp) {
      Collections.sort(messages, comparator);
    } else {
      Collections.sort(messages, new Comparator<Message>() {
        public int compare(Message o1, Message o2) {
          return -comparator.compare(o1, o2);
        }
      });
    }
  }
}
