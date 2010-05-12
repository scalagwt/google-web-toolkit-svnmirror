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
package com.google.gwt.sample.expenses.gwt.server;

import com.google.gwt.sample.expenses.gwt.client.DataGenerationService;
import com.google.gwt.sample.expenses.server.domain.Employee;
import com.google.gwt.sample.expenses.server.domain.Expense;
import com.google.gwt.sample.expenses.server.domain.Report;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Server-side implementation.
 */
public class DataGenerationServiceImpl extends RemoteServiceServlet implements
    DataGenerationService {
  
  private static final double AIRFARE = 600;

  private static final double BREAKFAST = 15;

  // Must be in sync with DESCRIPTIONS
  private static String[] CATEGORIES = {
      "Local Transportation", "Local Transportation", "Local Transportation",
      "Local Transportation", "Local Transportation", "Local Transportation",
      "Local Transportation", "Local Transportation", "Local Transportation",
      "Long Distance Transportation", "Long Distance Transportation",
      "Long Distance Transportation", "Long Distance Transportation",
      "Office Supplies", "Office Supplies", "Office Supplies",
      "Office Supplies", "Office Supplies", "Office Supplies",
      "Office Supplies", "Office Supplies", "Office Supplies",
      "Office Supplies", "Office Supplies", "Office Supplies",
      "Office Supplies", "Office Supplies", "Office Supplies",
      "Office Supplies", "Office Supplies", "Office Supplies",
      "Office Supplies", "Electronic Equipment", "Electronic Equipment",
      "Electronic Equipment", "Electronic Equipment", "Electronic Equipment",
      "Electronic Equipment", "Electronic Equipment", "Electronic Equipment",
      "Electronic Equipment", "Electronic Equipment", "Electronic Equipment",
      "Electronic Equipment", "Electronic Equipment", "Electronic Equipment",
      "Electronic Equipment", "Electronic Equipment", "Electronic Equipment",
      "Electronic Equipment", "Electronic Equipment", "Dues and Fees",
      "Dues and Fees", "Dues and Fees", "Dues and Fees", "Dues and Fees",
      "Dues and Fees", "Dues and Fees", "Dues and Fees", "Communications",
      "Communications", "Communications", "Communications", "Communications",
      "Communications", "Books", "Books", "Books", "Books", "Books", "Books",
      "Books", "Books", "Books", "Books", "Books", "Books", "Books", "Books",
      "Books", "Books", "Books", "Books", "Books", "Books", "Facilities",
      "Facilities", "Facilities", "Facilities", "Facilities", "Facilities",
      "Facilities", "Facilities", "Facilities", "Facilities", "Facilities",
      "Facilities", "Food Service", "Food Service", "Food Service",
      "Food Service", "Food Service", "Food Service", "Food Service",
      "Food Service", "Food Service", "Food Service", "Food Service",
      "Marketing", "Marketing", "Marketing", "Marketing", "Marketing",
      "Human Relations", "Human Relations", "Human Relations",
      "Human Relations", "Meals", "Meals", "Meals", "Lodging", "Lodging",
      "Lodging", "Lodging", "Lodging"};

  private static final String[] CITIES = {
      "New York, New York", "Los Angeles, California", "Chicago, Illinois",
      "Houston, Texas", "Phoenix, Arizona", "Philadelphia, Pennsylvania",
      "San Antonio, Texas", "San Diego, California", "Dallas, Texas",
      "Detroit, Michigan", "San Jose, California", "Indianapolis, Indiana",
      "Jacksonville, Florida", "San Francisco, California",
      "Hempstead, New York", "Columbus, Ohio", "Austin, Texas",
      "Memphis, Tennessee", "Baltimore, Maryland", "Charlotte, North Carolina",
      "Fort Worth, Texas", "Milwaukee, Wisconsin", "Boston, Massachusetts",
      "El Paso, Texas", "Washington, District of Columbia",
      "Nashville-Davidson, Tennessee", "Seattle, Washington",
      "Denver, Colorado", "Las Vegas, Nevada", "Portland, Oregon",
      "Oklahoma City, Oklahoma", "Tucson, Arizona", "Albuquerque, New Mexico",
      "Atlanta, Georgia", "Long Beach, California", "Brookhaven, New York",
      "Fresno, California", "New Orleans, Louisiana", "Sacramento, California",
      "Cleveland, Ohio", "Mesa, Arizona", "Kansas City, Missouri",
      "Virginia Beach, Virginia", "Omaha, Nebraska", "Oakland, California",
      "Miami, Florida", "Tulsa, Oklahoma", "Honolulu, Hawaii",
      "Minneapolis, Minnesota", "Colorado Springs, Colorado",
      "Arlington, Texas", "Wichita, Kansas", "St. Louis, Missouri",
      "Raleigh, North Carolina", "Santa Ana, California",
      "Anaheim, California", "Cincinnati, Ohio", "Tampa, Florida",
      "Islip, New York", "Pittsburgh, Pennsylvania", "Toledo, Ohio",
      "Aurora, Colorado", "Oyster Bay, New York", "Bakersfield, California",
      "Riverside, California", "Stockton, California", "Corpus Christi, Texas",
      "Buffalo, New York", "Newark, New Jersey", "St. Paul, Minnesota",
      "Anchorage, Alaska", "Lexington-Fayette, Kentucky", "Plano, Texas",
      "St. Petersburg, Florida", "Fort Wayne, Indiana", "Glendale, Arizona",
      "Lincoln, Nebraska", "Jersey City, New Jersey",
      "Greensboro, North Carolina", "Norfolk, Virginia", "Chandler, Arizona",
      "Henderson, Nevada", "Birmingham, Alabama", "Scottsdale, Arizona",
      "Madison, Wisconsin", "Baton Rouge, Louisiana",
      "North Hempstead, New York", "Hialeah, Florida", "Chesapeake, Virginia",
      "Garland, Texas", "Orlando, Florida", "Babylon, New York",
      "Lubbock, Texas", "Chula Vista, California", "Akron, Ohio",
      "Rochester, New York", "Winston-Salem, North Carolina",
      "Durham, North Carolina", "Reno, Nevada", "Laredo, Texas"};

  private static final String[] DEPARTMENTS = {
      "Operations", "Engineering", "Finance", "Marketing", "Sales"};

  // Must be in sync with CATEGORIES
  private static String[] DESCRIPTIONS = {
      "Train Fare", "Taxi Fare", "Monorail", "Water Taxi", "Bus Fare",
      "Bicycle Rental", "Car Rental", "Limousine Service", "Helicopter",
      "Airplane Ticket", "Bus Ticket", "Train Ticket", "Car Rental",
      "Paperclips", "Stapler", "Scissors", "Paste", "Notebooks", "Pencils",
      "Whiteboard Markers", "Tissues", "Pens", "Copier Paper", "Legal Pad",
      "Rubber Bands", "Binder Clips", "Scotch Tape", "Masking Tape",
      "Tape Dispenser", "Highlighter", "Staples", "File Folders", "Headphones",
      "Workstation", "Laptop", "USB Cable", "Electronic Plunger",
      "Serial Cable", "KVM", "Video Cable", "Docking Station", "Headset",
      "Speakers", "Keyboard", "Mouse", "UPS", "Hard Drive", "CD-ROM Drive",
      "Power Cord", "Extension Cord", "Surge Protector", "ACM Membership",
      "IEEE Membership", "Google I/O Ticket", "Parking Ticket",
      "Other Professional Association", "Conference Fee", "Trade Show Fee",
      "Bank Fee", "Telephone", "Internet", "Mobile", "Phone Card",
      "Satellite Phone", "Cable TV", "AJAX", "Java", "C++", "C#", "Python",
      "Google Go", "Perl", "Visual Basic", "Software Engineering", "Windows",
      "UNIX", "Linux", "Apple", "Android", "iPhone", "Blackberry", "Mobile",
      "Software Design", "Marketing", "Management", "Toilet Paper",
      "Paper Towels", "Cleaning Supplies", "Cleaning Contractor", "Repairs",
      "Updates", "Exterminator", "Plant Care", "Decoration", "Furniture ",
      "Reading Material", "Trash Bags", "Coffee Cups", "Coffee Stirrers",
      "Coffee Lids", "Condiments", "Coffee Maker Supplies",
      "Coffee Maker Maintenance", "Coffee Beans", "Tea", "Bottled Drinks",
      "Snacks", "Straws", "Flyers", "Posters", "Booth", "Meeting  Expenses",
      "Design Consultant", "Candidate Travel", "Recruiting Expenses",
      "Outreach", "Training", "Self", "Co-Workers", "Customers", "Hotel",
      "Motel ", "Holiday Inn", "Private Apartment", "Corporate Apartment"};

  private static final double DINNER = 60;

  // 11% of females hyphenate their last names
  private static final double FEMALE_HYPHENATE = 0.11;

  private static List<Double> femaleNameFreqs = new ArrayList<Double>();

  private static List<String> femaleNames = new ArrayList<String>();

  private static final double HOTEL = 300;

  private static final DataGenerationServiceImpl instance = new DataGenerationServiceImpl();

  private static List<Double> lastNameFreqs = new ArrayList<Double>();

  private static List<String> lastNames = new ArrayList<String>();

  private static final Logger log = Logger.getLogger(DataGenerationServiceImpl.class.getName());

  private static final double LUNCH = 25;

  // 2% of males hyphenate their last names
  private static final double MALE_HYPHENATE = 0.02;

  private static List<Double> maleNameFreqs = new ArrayList<Double>();

  private static List<String> maleNames = new ArrayList<String>();

  private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

  private static final long MILLIS_PER_HOUR = 60L * 60L * 1000L;

  private static final String[] NOTES = {
      // Some entries do not have notes.
      "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "Need approval by Monday", "Bill to widgets project", "High priority",
      "Review A.S.A.P."};

  private static final String[] PURPOSES = {
      "Spending lots of money",
      "Team building diamond cutting offsite",
      "Visit to Istanbul",
      "ISDN modem for telecommuting",
      "Sushi offsite",
      "Baseball card research",
      "Potato chip cooking offsite",
      "Money laundering",
      "Donut day",
      "APPROVED BY Amy Mobley",
      "For Michael Neal",
      "Customer meeting with Maureen Rosado",
      "See Tommy Perry",
      "Approved by George Hamilton",
      "Discussed with Sandra Simpson",
      "Approved by James Mills",
      "Cheryl Munoz",
      "Approved Earl Butler",
      "approved John Gutierrez",
      "Discussed with Celeste Baker",
      "ran by Daryl Kent",
      "Mary Jensen",
      "with Mary Hicks",
      "with Casey Clay",
      "With Lillian Dennis",
      "customer meeting with Joshua Cobb",
      "Asked Joseph Ray",
      "approved Olivia Torres",
      "meeting with Viola Boggs",
      "Ran by Kelly Hopkins",
      "Paul Lee",
      "For Otto Vasquez",
      "met with Steven Price",
      "met with Beulah Brown",
      "CUSTOMER MEETING WITH Cameron Bishop",
      "approved Joe Dawson",
      "approved Tonya Hamm",
      "for Evelyn Warren",
      "w/ Elaine Martinez",
      "meeting with Stephanie Gonzales",
      "Approved by Christopher Thompson",
      "Caroline Vazquez",
      "With Justin Davis",
      "Approved by Angelica Irwin",
      "customer meeting with Clarence Macias",
      "ran by Terry King",
      "customer meeting with Frederick Carroll",
      "lunch with Catherine Nelson",
      "see Todd Dejesus",
      "Aida Deleon",
      "with Mike Jenkins",
      "Approved by Celia Dennis",
      "w/ Linda Cox",
      "Ran by Bobbie Park",
      "met with Teresa Wiley",
      "Lunch with Barbara Cordova",
      "Meeting with Carol Gonzales",
      "meeting with Irene Perry",
      "Approved by Kimberly Johnson",
      "FOR Barbara Martin",
      "lunch with Gregg Williams",
      "Approved by Kevin Griggs",
      "ran by Ruben Kelley",
      "For Kenneth Thompson",
      "asked Julia Ferguson",
      "For Kristina Anderson",
      "Fred Graham",
      "For Sean Banks",
      "meeting with Don Nichols",
      "meeting with Antonio Howard",
      "see Walter Wiggins",
      "ran by Amy Marshall",
      "with Beverly Pace",
      "w/ Stacy Roy",
      "meeting with Thomas Williams",
      "customer meeting with Belinda Nelson",
      "with Pierre Price",
      "See Hilda Vaughn",
      "met with Kara Rogers",
      "For Frances Morris",
      "customer meeting with George Dalton",
      "meeting with Lindsey Webb",
      "meeting with Theresa Dunlap",
      "See Stephanie Benson",
      "W/ John Pope",
      "Approved by Alejandro Harris",
      "asked James Lopez",
      "customer meeting with Dorothy Colon",
      "customer meeting with Tracy Norman",
      "Lunch with Kelly Benton",
      "Approved by Edmund Ortiz",
      "Met with Joel Martinez",
      "See James Hendricks",
      "lunch with Tonya Brown",
      "approved by Sarah Newman",
      "meeting with Bonnie Petersen",
      "discussed with John Wagner",
      "approved by Mark Salinas",
      "customer meeting with Jack Duran",
      "Approved by Sherry Rojas",
      "w/ Mary Decker",
      "See Theresa Griggs",
      "with Robert Humphrey",
      "approved by Jay Lawson",
      "for Mark Bell",
      "ran by Darrell Wheeler",
      "With Judy Fields",
      "Discussed with William Nelson",
      "Approved by Natalie Mcclain",
      "W/ Angel Manley",
      "Asked Barbara Martin",
      "James Andrade",
      "w/ James Norris",
      "discussed with Richard Bowman",
      "For Alfred Ruiz",
      "For Bobby Johnson",
      "w/ Daniel Price",
      "ran by Gail Rogers",
      "meeting with Jeffrey Beck",
      "lunch with James Roman",
      "for Brian Dickerson",
      "Charles Wong",
      "Louis Morrison",
      "met with Edgar Lopez",
      "asked Steven Martinez",
      "customer meeting with Felicia Salazar",
      "ran by Marie Brown",
      "w/ Steve Curry",
      "ran by Leslie Dorsey",
      "Approved by Martha Edwards",
      "met with David Moran",
      "lunch with Larry Lynch",
      "see Douglas Mcclain",
      "approved by Howard Wells",
      "W/ Franklin Adams",
      "approved by Jennifer Hernandez",
      "approved Katherine Krueger",
      "Approved by Linda Taylor",
      "See Jean Miller",
      "Lunch with Janet White",
      "with Michelle Vasquez",
      "meeting with Edward Clarke",
      "with Micheal Collier",
      "ran by Dolores Gonzalez",
      "For William Hubbard",
      "for Scott Mitchell",
      "discussed with John Murray",
      "with Charles Jones",
      "See Vicki Boucher",
      "Approved by Vincent Lewis",
      "discussed with Charles Minor",
      "For Terry Lawrence",
      "For Betty Armstrong",
      "James Gaines",
      "Meeting with Mary Anthony",
      "See James Crowley",
      "w/ Gloria Herrera",
      "Met with Scott Clemons",
      "discussed with Mary Smith",
      "SEE Mary King",
      "For Leroy Brandon",
      "discussed with Gloria Hinton",
      "approved Teresa Thompson",
      "discussed with Elizabeth Johnson",
      "See Jose Ibarra",
      "approved by Mary Reese",
      "asked Donna Barnett",
      "ran by Billy Finch",
      "w/ Nancy Hart",
      "lunch with Martha Smith",
      "See Mark Becker",
      "with Ruben York",
      "lunch with Jerome Palmer",
      "for Melissa Yarbrough",
      "William Powers",
      "with James Fletcher",
      "approved Edward Snyder",
      "Yvonne Harris",
      "Lunch with Ramon Smith",
      "discussed with Anthony Meadows",
      "For Nancy Taylor",
      "w/ David Hartman",
      "approved Ida Jackson",
      "See Chris Blackwell",
      "APPROVED BY Raymond Mueller",
      "Approved by James Shaw",
      "Ran by Mark Garcia",
      "discussed with Linda Rojas",
      "approved Debbie Little",
      "discussed with John Jones",
      "For Krista Benson",
      "For Felicia Davidson",
      "Lawrence Davis",
      "Vicky Anderson",
      "W/ Sheri Cooper",
      "w/ Andrea King",
      "lunch with Dennis Wright",
      "Asked Jeremy Fowler",
      "w/ Leslie Graves",
      "lunch with Justin Davis",
      "See Arthur Finley",
      "For Michael Matthews",
      "See Adam Rodriguez",
      "approved Joy Childs",
      "approved Carlos Holt",
      "lunch with Michael Tracy",
      "customer meeting with Amanda Campbell",
      "See Thomas Oliver",
      "customer meeting with Emily Thompson",
      "SEE Tony West",
      "Discussed with Edward Foley",
      "approved by Emma Meyer",
      "approved Brandy King",
      "With Clifton Davis",
      "met with Johnnie Patrick",
      "met with Jessica Weber",
      "asked Carolyn Johnson",
      "approved Kim Warren",
      "w/ Kimberly Foley",
      "see Joseph Mcgee",
      "Mario Taylor",
      "met with Kenny Hampton",
      "See Sarah Wilson",
      "customer meeting with Autumn Anderson",
      "See Rebecca Walker",
      "lunch with Amber Lowe",
      "discussed with Myrtle Crowley",
      "approved by Doris Lawson",
      "approved Wallace Goodman",
      "customer meeting with Amy Reese",
      "meeting with Frederick Carroll",
      "discussed with Roosevelt Wright",
      "see Jose Clark",
      "see Erica Jones",
      "LUNCH WITH Regina Munoz",
      "Met with David Morrison",
      "See Leona Adams",
      "for Vicki Ward",
      "for Ismael English",
      "See Linda Nash",
      "ran by Richard Best",
      "with Glen Stewart",
      "asked Gladys Henson",
      "ran by Tricia Gardner",
      "lunch with Dorothy Johnson",
      "approved by Shirley Taylor",
      "with Stacy Moody",
      "For Edmond Williams",
      "with Shaun Phillips",
      "lunch with Janet Bowen",
      "customer meeting with Sandra Anderson",
      "Meeting with Douglas Hollis",
      "with Mandy Warner",
      "Approved by Mark Garcia",
      "approved Betty Kirkland",
      "ran by Vicki Thomas",
      "with Georgia Mckinney",
      "discussed with David Abbott",
      "approved Chester Everett",
      "DISCUSSED WITH Jane Harper",
      "Meeting with Melvin Richardson",
      "for Viola Roy",
      "approved by Dean Peck",
      "Approved by Paulette Rollins",
      "meeting with Manuel Hale",
      "For Kimberly White",
      "discussed with Martha Nash",
      "For Stacey Johnson",
      "ran by Carol Gonzales",
      "See Elaine Thomas",
      "meeting with Josh Smith",
      "customer meeting with Don Bender",
      "Meeting with Ina Reed",
      "For Keith Cobb",
      "approved by Paul Rodgers",
      "approved by Eleanor Ramirez",
      "For Janice Cooper",
      "asked William Tanner",
      "Discussed with Minnie Blackburn",
      "ran by Rose Clark",
      "customer meeting with Brandon Marshall",
      "approved David Jimenez",
      "approved by Helen Ayers",
      "W/ Eileen Martinez",
      "FOR Benjamin Williams",
      "Meeting with Sharon Mcdonald",
      "meeting with Thomas Bowers",
      "met with Marion Wilson",
      "Lunch with Dennis Mitchell",
      "Approved by Alejandro Wheeler",
      "meeting with Viola Bradley",
      "For Charles Blevins",
      "approved Tammie Phillips",
      "customer meeting with Norman Johnson",
      "see Debbie Cox",
      "Asked Joan Williams",
      "See Rodney Branch",
      "discussed with Michael Mayer",
      "Approved by Kendall Taylor",
      "approved David Rivera",
      "with Joy Valentine",
      "customer meeting with Edward Workman",
      "for Rebecca Scott",
      "asked Ivy White",
      "with Glenda Peterson",
      "Paul Smith",
      "approved Antonio Curry",
      "approved Leonard Knight",
      "See Amy Banks",
      "LUNCH WITH Emmanuel Morales and John Williams",
      "customer meeting with Benjamin Lloyd and Jeffrey Prince",
      "customer meeting with William Powers and Claude Wells",
      "lunch with David Martin and Keith Campbell",
      "customer meeting with Charles Mckay and Elizabeth Patterson",
      "for Larry Soto and Felipe Smith",
      "met with Carlos Gray and John Nelson",
      "For Abby White and Debra Hunter",
      "with Keith Davidson and George Becker",
      "discussed with Thomas Adams and Betty Garcia",
      "met with Juan Lee and Mary Johnson",
      "For Raymond White and Carrie Hunt",
      "lunch with Juanita Richardson and Rachel White",
      "discussed with Samuel Wade and Martha Gomez",
      "Approved by Dianna Gillespie and Elizabeth Gordon",
      "For John Moody and Vicki Ward",
      "approved Yvonne Pierce and Pearl Landry",
      "For Darlene Bell and Steven Johnson",
      "Jennifer Campbell and Michelle Reed",
      "see Donald Kramer and Timothy Duke",
      "see Cameron Hunt and Stacey Jackson",
      "meeting with Eric Silva and Bruce Andrade",
      "discussed with Robert Cannon and Ann Johnson",
      "customer meeting with Leonard Horton and Mark Brady",
      "approved Adam Noble and Jacquelyn Byrd",
      "SEE Francis Cowan and Robert Flores",
      "customer meeting with Doris Schmidt and Rodolfo Young",
      "ran by John Warren and Laura Dixon",
      "see Antonio Nelson and Sarah Floyd",
      "approved by Frances Smith and Bryan Velazquez",
      "Corinne Elmore and William Edwards",
      "w/ Kelly Scott and Susan Gutierrez",
      "Discussed with Dolores Hicks and Claudia Freeman",
      "customer meeting with Addie Kennedy and Raymond Thompson",
      "Asked Albert Kramer and Marie Brown",
      "lunch with Charles Casey and Bette Knight",
      "Met with Kathryn Mathews and William Thompson",
      "with Ida Vega and Mike Miller",
      "asked Melvin Edwards and Christopher Richardson",
      "with Barbara Anderson and Joseph Oliver",
      "Approved by Timothy Bacon and Dora Bonner",
      "w/ Elizabeth Brooks and Jose Burt",
      "Discussed with Rhonda Fritz and Clint Brown",
      "w/ John Mitchell and Kara Rogers",
      "with Carla Lowe and Lillie Hodges",
      "lunch with Kathleen Strickland and Shaun Williams",
      "For Bradley Todd and Kevin Jones",
      "discussed with Patricia Ramirez and Trevor Fleming",
      "With Margaret Reese and Doris Deleon",
      "discussed with Belinda Roberts and Stanley Thomas",
      "MEETING WITH Shanna Rogers and Judy Sanchez",
      "Customer meeting with Veronica James and Lisa Brown",
      "lunch with Wayne Ramsey and Joan Powell",
      "Approved by Alan Thompson and Rose Goodman",
      "Customer meeting with Arnold Dixon and Robert Jones",
      "See Maryann Hastings and Carol Booth",
      "Meeting with Carroll Daniel and Michael Gibbons",
      "ran by Mary Thornton and Dustin Reed",
      "for Jason Washington and Sara Brown",
      "customer meeting with Angela Kirk and Clark Erickson",
      "lunch with Louise King and Elizabeth Carroll",
      "approved Jane Hernandez and Matthew Smith",
      "Asked Patricia Smith and Julia Ferguson",
      "asked Alice Thomas and Barry Perez",
      "Approved by Marisa Lopez and Lee Holmes",
      "approved Philip Lewis and Alice Nance",
      "for Marvin Herron and Kenneth Flowers",
      "See Frank Scott and Katherine Collier",
      "approved Sean Finley and Ruth Hernandez",
      "discussed with Bethany Bryant and Jaime Mooney",
      "Customer meeting with Franklin Horton and Kevin Harris",
      "met with Jack Roberts and James West",
      "Asked Ann Smith and Beverly Davis",
      "approved by Terry Gross and Eric Brown",
      "met with Danny Pryor and Dan Rutledge",
      "MET WITH Ernest Lawson and Alfred Velasquez",
      "Discussed with Samuel Crawford and Michael White",
      "w/ Maria Brown and Paul Smith",
      "See Michelle Spencer and Barbara Johnson",
      "For Margaret Jenkins and Pierre Harper",
      "approved by George Mooney and Sean Olson",
      "See Dan Thompson and Emma Starr",
      "Approved by Ann Gomez and Caroline Wheeler",
      "See James Thompson and Katherine Barton",
      "approved Barry Nicholson and Thomas Haynes",
      "ran by Curtis Fitzgerald and Steven Vargas",
      "approved by Gary Jones and Felix Black",
      "ran by Phil Thomas and Linda Dotson",
      "Jeffery Merritt and Andrew Holt",
      "for Pamela Curtis and Jasmine Coleman",
      "w/ Roland Johnston and James York",
      "asked Beverly Richards and Lisa Brown",
      "ran by John Ellis and Beverly Sanders",
      "Lunch with Marilyn Cox and Joyce Clay",
      "w/ Joanne Rodriquez and Phillip Wilson",
      "asked Darlene Bell and Rodney Branch",
      "asked Gertrude Orr and Tamara Smith",
      "approved James Guy and Cindy Harper",
      "met with Brandy King and Brandon Roberts",
      "Lillie Moore and Martin Jackson",
      "DISCUSSED WITH Pamela Welch and Ron Atkins",
      "for Carla Mccarthy and Richard Livingston",
      "approved Jonathan Moore and Elizabeth Williams",
      "with Shirley Anderson and Earl Butler",
      "Meeting with Janet Long and Robert Boucher",
      "W/ Debbie Brown and Jeff Collier",
      "ran by Todd Welch and Tim Gonzalez",
      "w/ James Cline and James Daniel",
      "Customer meeting with Teresa Evans and Robert Humphrey",
      "approved by Mark Cortez and Judy Russell",
      "approved Michael Simpson and Angela Harvey",
      "meeting with Helen Richardson and Andrea Barnett",
      "Approved by Jasmine Rogers and Steven Serrano",
      "customer meeting with Timothy Stephens and Willie Wallace",
      "Gerardo Brown and Ronald Bryant",
      "approved by Rocky Daniel and James Thomas",
      "meeting with Barbara Ratliff and Dorothy Gallagher",
      "Lunch with Aaron Little and Bobbie Ellis",
      "customer meeting with Henry Peterson and Lora Adams",
      "Approved by Thomas Haynes and Bessie Escobar",
      "Alicia Mitchell and Anthony Bullock",
      "Lunch with Katrina Holt and Robert Coleman",
      "ran by Elisabeth Marshall and Michael Ferguson",
      "asked Curtis Wilkins and Donald Lee",
      "lunch with Stacy Lyon and Cheri Hughes",
      "SEE Nathan Perez and Leah Hill",
      "Approved by Rachelle Todd and Erica Jones",
      "with Theresa Dunlap and Raymond Mueller",
      "Approved by Kyle Fulton and Rebecca Young",
      "Meeting with Debbie Brown and Elizabeth Johnson",
      "w/ Shirley Castillo and Paul Cohen",
      "W/ Pedro Gibson and Winifred Barber",
      "For Marjorie Wilson and Mary Moreno",
      "Lunch with Francisco Thomas and Roger King",
      "ran by Linda Wang and Paul Smith",
      "See Jan Burnett and Elaine Martinez",
      "with Eugene Jones and Lorraine Flanagan",
      "approved Steven George and Edgar Snyder",
      "see Tim Mitchell and Tony Bruce",
      "customer meeting with Roberto George and Lynn Edwards",
      "with James Forbes and Maria Holley",
      "meeting with Eva Thomas and Sharon Sanchez",
      "Approved by Frank Potts and Sarah Cooper",
      "See Elizabeth Boone and Harold Davis",
      "Approved by Hector Cross and Ray Turner",
      "w/ Rachel White and Lorraine Briggs",
      "For Catherine Simpson and Carrie Whitaker",
      "John Murray and Heather Munoz",
      "approved William Barron and Jason Wilson",
      "met with Edward Sloan and Sherry Davis",
      "FOR James Morrison and Shannon Cowan",
      "Ran by Michael Johnson and Michael Turner",
      "with David Jensen and James Thomas",
      "approved by Janet Jones and Andre Gutierrez",
      "meeting with Drew Turner and Kathy Allen",
      "For Wilbur Sanchez and Kenneth Cox",
      "Lunch with John Plummer and Michael Richardson",
      "with Bertha Bryant and Eric Brown",
      "Terri Long and John Wagner",
      "approved Catherine Cox and Gina Hutchinson",
      "Willie Clark and Gloria Johnson",
      "Meeting with Eduardo Watkins and Lynne Gomez",
      "for Carol Anderson and Alvin Parker",
      "Asked Herbert Mcdonald and Dennis Mitchell",
      "Approved by Jeffrey Hernandez and Mark Cortez",
      "see Barry Miller and Debra Miller",
      "see Harold James and Bert Roberts",
      "asked Michael Adams and Francis Jefferson",
      "Approved by Mary Mcmahon and Lucinda Blanchard",
      "ran by Antonio Nelson and Carolyn Wilkerson",
      "met with Lynda Franks and Carol Anderson",
      "see Eduardo Barr and Michael Sanchez",
      "See Jason Porter and Kathy Rogers",
      "met with Tammie Phillips and Cheryl Garcia",
      "For Antonio Rosa and Ronald Burgess",
      "APPROVED BY George Cunningham and Jerry Hernandez",
      "Meeting with Claude Anderson and Anthony Mercer",
      "See William Shelton and David Jimenez",
      "ran by Roy Johnson and Robert Miller",
      "w/ Irene Perry and Micheal Anderson",
      "met with Omar Page and Edith Soto",
      "For Sheila Meadows and Cynthia Roach",
      "met with Marisa Polk and Anna Roberts",
      "for Heather Hernandez and Deanna Dunlap",
      "ran by Erik Sears and Constance Gray",
      "w/ Susan Gutierrez and Gerald Erickson",
      "See Ted Peterson and Wade Hogan",
      "ran by Linda Sanders and Frank Becker",
      "ran by Tim Schwartz and Leslie Poole",
      "See Eric Cole and Daniel Bennett",
      "Approved by David Thomas and Josephine Jones",
      "asked Gladys Shannon and Michael Garcia",
      "discussed with Sharon Smith and Robin Hudson",
      "approved Margaret Kendrick and George Cunningham",
      "ran by Michael Sutton and Michael Simpson",
      "See James Mack and David Chang",
      "discussed with Melanie Ward and Daniel Saunders",
      "with Rosa Duncan and Kelly Scott",
      "asked Cathy West and Lindsay Davis",
      "For Orlando Terrell and Cynthia King",
      "APPROVED BY Kelly Morris and Jonathan Becker",
      "discussed with James Burns and Peggy Pugh",
      "asked Jeremiah Parks and Jimmy Johnson",
      "ran by Michelle Spencer and Matthew Ward",
      "customer meeting with George Moore and Tiffany Mack",
      "approved by Edward Hurley and Mark Cortez",
      "For Richard Allen and Sheri Cooper",
      "with Sarah Floyd and Mary Payne",
      "Asked Stacy Lyon and Mary Smith",
      "Approved by Daniel Smith and Jason Smith",
      "asked Robert Gonzales and Gabrielle Johnson",
      "Customer meeting with Gwendolyn Brown and Thomas Atkins",
      "meeting with Joshua Newton and Roy Cole",
      "approved by Brian Simmons and Brian Spencer",
      "For Thomas Thornton and Bertha Dixon",
      "with David Livingston and Tim Hoffman",
      "Met with Jennifer Fowler and Mary Molina",
      "lunch with Josephine Sears and Christopher Green",
      "lunch with John Jones and Nicole Stein",
      "lunch with Jennifer Wilson and Ellen Lambert",
      "ran by Clyde Robertson and George Cunningham",
      "with George Reese and Vicky Anderson",
      "Approved by Lacey Robinson and Bonnie Brown",
      "Approved by Glenn Morton and Homer Barker",
      "customer meeting with Vanessa Willis and Fred Graham",
      "CUSTOMER MEETING WITH Michael Walters and Roy Thompson",
      "Asked Sallie Peterson and Rachelle Conley",
      "ran by Frances Henson and Steven Nguyen",
      "See Marjorie Davis and Mary Morgan",
      "with Robert Cannon and Sterling Avery",
      "Dorothy Padilla and Raymond Morgan",
      "Monica Gregory and Genevieve Contreras",
      "w/ Maria Warner and Brandi Dyer",
      "W/ Richard Scott and Johnny Lewis",
      "Approved by Dixie Swanson and Felipe Smith",
      "lunch with Richard Castro and April Jones",
      "for Barbara Martin and Richard Fuller",
      "Judith Scott and John Wallace",
      "asked Emma Gilbert and Rodney Morgan",
      "see Wendy Washington and Laura Farley",
      "approved Monica Holden and Jessie Odell",
      "asked Terry Williams and Jane Robinson",
      "See Maria Mosley and Carmen Herman",
      "Discussed with Erin Williams and Erin Warren",
      "For Joshua Newton and Jeffery Tanner",
      "w/ David Valentine and Lisa Caldwell",
      "see Julie Rodriguez and Kevin Parks",
      "ran by Bryan White and Sandra Simpson",
      "discussed with Stanley Smart and Robert Copeland",
      "For Charles Barber and Yolanda Ellison",
      "WITH Gwen Blackburn and Sammy Miller",
      "with Kevin Griggs and Sandra Byrd",
      "discussed with Jack Duran and Christine Gross",
      "customer meeting with Leslie Gomez and Kim Warren",
      "See Mary Mcmahon and Eleanor Ramirez",
      "met with Melissa Yarbrough and Bette Knight",
      "Approved by Josephine Johnson and Pamela Hansen",
      "meeting with Deanna Clifton and Margaret Taylor",
      "Approved by Marvin Alvarez and Janice Baker",
      "approved Cassandra Buchanan and Oscar Cooper",
      "See Daniel Green and Catherine Schultz",
      "For Manuel Hale and Angela Carter",
      "See Paul Brady and Victor Roberts",
      "approved Rebecca Guzman and Robert Perez",
      "ran by Enrique Harris and Steven Soto",
      "ran by Jennifer Gray and Rodney Branch",
      "met with Laura Walker and James Jones",
      "Asked Jay Vega and Bruce Campbell",
      "approved by Belinda Roberts and Betty Rodriguez",
      "Approved by Amanda Clark and Larry Duncan",
      "For Robert Rivera and Bertha Dixon",
      "Approved by Richard Foreman and Kelly Fuentes",
      "With Emily Fisher and John Cantrell",
      "Asked Keith Cobb and Jill Bryant",
      "For George Moore and Margaret Reese",
      "DISCUSSED WITH Robert Bell and Jack Duran",
      "lunch with Gary Horton and James Forbes",
      "approved by Scott Mitchell and Samuel Vargas",
      "customer meeting with Jerald Williams and Kelly Morris",
      "customer meeting with Mary Green and Curtis Wilkins",
      "customer meeting with Juan Waters and William Wiley",
      "For Marcus Kirby and Laura Martinez",
      "met with Allison Torres and Troy Moses",
      "Approved by Charles Jackson and Kelly Benton",
      "met with James Thompson and Francis Dudley",
      "meeting with Thomas Merritt and Matthew Gray",
      "asked Bernard Holman and Craig Williams",
      "met with Bruce Hughes and Saul Wiggins",
      "For Carl Clay and John Lehman",
      "Approved by Frederick Carroll and Ethel Williams",
      "Approved by Lottie Ritter and Kelly Aguilar",
      "for Stephen Miller and Thomas Ruiz",
      "See Kathleen Kelly and Justin Jackson",
      "see Margaret Phelps and Todd Williams",
      "meeting with Calvin Carr and Dawn Crane",
      "Approved by Julia Brown and Melissa Hall",
      "asked Carol Hodges and Anna Leonard",
      "see Gary Watkins and Stephanie Thornton",
      "For Anthony King and Diane Flynn",
      "Approved by Oscar Larsen and Timothy Stephens",
      "FOR Don Hood, Esteban James and Jennifer Padilla",
      "ran by Sean Olson, Gerard Browning and Norma Armstrong",
      "ran by Shelly Kim, Betty Cohen and Betty Bowers",
      "approved by Stacy James, Stanley Russell and Cary Hines",
      "With Michelle Davis, Jacquelyn Byrd and Joseph Walter",
      "ran by Aubrey Norris, Tina Cleveland and Joe Washington",
      "See Michael Beard, Fredrick Clemons and Richard Barnett",
      "met with Lillie Moore, Kyle Welch and Don Bender",
      "discussed with James Porter, Elaine Martinez and Joel Curry",
      "W/ Joseph Rodriguez, Debbie Cox and Mary Bradley",
      "See Walter Thomas, Claude Wells and Charles King",
      "W/ Amy Banks, Helen Weiss and Mary Jones",
      "see Robert Hawkins, Ola Flores and Donna Ponce",
      "discussed with James Woods, Theresa Thompson and Woodrow Childers",
      "Approved by Manuel Hale, Inez Acosta and Cassie Key",
      "approved by Tammy Patterson, James Brown and Steven Decker",
      "for William Kennedy, Demetrius Magee and Jeanette Wagner",
      "Approved by Marvin Terry, George Moore and Oscar Ford",
      "approved Carl Green, Margaret Savage and Emma Cotton",
      "approved by Darla Jones, Martin Jackson and Jason Johnson",
      "approved Anthony Hess, Barbara Parker and Gail Clay",
      "Ran by Lupe Thompson, Jacquelyn Byrd and Benjamin Williams",
      "Approved by Yolanda Cantrell, Deborah Vasquez and Judith Perez",
      "Approved by William Hoover, Stephen Rivera and Ann Taylor",
      "Lunch with Georgia Mckinney, Samantha Hopkins and Ana Stinson",
      "W/ Heather Rodriguez, Richard Lee and Rachel Goldstein",
      "lunch with Deborah Baldwin, Sylvia Morris and Edward Alvarez",
      "Discussed with David Cook, Kathleen Anderson and Phillip Chambers",
      "see Emily Miller, Brian Simmons and Denise Smith",
      "lunch with Jeffrey Nguyen, Veronica Belcher and Jessie Odell",
      "asked Tiffany Dunn, Karen Baker and Joseph Gray",
      "approved Marvin Terry, Jan Burnett and Sue Campbell",
      "Discussed with Bertha Bryant, Matthew Parsons and Jason Smith",
      "ran by Amy Lang, Bobby White and Connie Bowers",
      "approved by Cecilia Williams, Lloyd Taylor and Raymond Thompson",
      "met with Evelyn Wyatt, Janet Chavez and Camille Adkins",
      "For Aaron Williams, Elisabeth Marshall and Roger Stuart",
      "see Rosa Kennedy, Blake Jones and Joshua Jackson",
      "For Minnie Shaw, Richard Barnett and Elizabeth Williams",
      "ran by Angelina Kirby, Charles Schultz and Robert Cole",
      "approved Timothy Horn, Lisa Lynch and Rocky Daniel",
      "Approved by Scott West, Michael Sutton and Matthew Roberson",
      "for Manuel Roberts, Diana Mack and Dan Petersen",
      "Ralph Harrington, Franklin Horton and Jermaine Donaldson",
      "approved Margaret Nunez, Randy Moore and Cathy Taylor",
      "Customer meeting with Nathan French, Harold James and Kirk Flores",
      "w/ Beverly Richards, Rachel Goldstein and Eddie Coleman",
      "meeting with James Morrison, Lillian Weber and Allen Childers",
      "customer meeting with Angelo Lopez, Juanita Black and Connie Collins",
      "customer meeting with Donna Obrien, Samuel Sweet and Sharon Franklin",
      "APPROVED BY Roger Humphrey, James Allen and Matthew Warren",
      "approved by Jennifer Green, Sarah Walton and Bridget Stone",
      "Approved by Edna Garza, Brandon Wiley and Richard Hill",
      "customer meeting with Micheal Drake, Glenn Whaley and David Gibson",
      "for Joy Childs, Ethel Dye and John Donahue",
      "For Eric Lewis, Brooke Williams and Tamika Williams",
      "ran by John Jones, Frank Scott and Garrett Ellis",
      "ran by Joshua Palmer, Oliver Sweet and Lillian Chavez",
      "asked Joshua Lewis, Alvin Dudley and Leonard Hunt",
      "see Jorge Eaton, Jason Smith and Jessica Rivera",
      "For Maxine Murray, Jay Lawson and Viola Robinson",
      "see Kenneth Bowers, Bruce Campbell and Andrew Williams",
      "for Beverly Duncan, Lisa Mack and Scott Thompson",
      "customer meeting with Maria Robertson, Mike Barnes and Sonya Alvarez",
      "Discussed with Dorothy King, Kenny Hampton and Regina Ramirez",
      "Bobby White, Frances Montgomery and Joyce Kerr",
      "Anna Leonard, Patricia Anderson and Margaret Coleman",
      "approved by Janet White, Marguerite Peters and Carl Evans",
      "Approved by Tim Chandler, Geraldine Wise and David Gibson",
      "For Brandon Russo, Kenneth Cox and William Crabtree",
      "ran by Kerry Thompson, Marian Mcdonald and Susan French",
      "lunch with Daisy Johnson, Otto Vasquez and Todd Welch",
      "asked Mary Smith, Patrick Parks and Robert Boucher",
      "Joan Marsh, Julie Rodriguez and Joyce Perez",
      "approved by Robert Martinez, Michelle Jones and Gary Jones",
      "DISCUSSED WITH Mildred Cowan, Mattie Kelly and Darrel Jenkins",
      "customer meeting with John Ware, Carolyn Mendoza and Frances Fitzgerald",
      "meeting with Christine Hutchinson, Phillip Cole and Marcia Martin",
      "Justin Hill, David Williams and Robert Copeland",
      "customer meeting with Thomas Atkins, Randy Moore and Andrew Marsh",
      "See Melissa Davenport, Diane Johnson and Santiago Brown",
      "Discussed with Jeffrey Collier, Helen Thomas and Shawn Quinn",
      "asked Janet White, Elizabeth Benson and Clint Brown",
      "approved Estelle Harris, Louis Irwin and Kristina Anderson",
      "See Diane Peterson, Jason Davis and Elaine Montgomery",
      "For Jerry Tucker, Lola Allen and Lorenzo Coleman",
      "lunch with Tiffany Ramsey, Janice Morgan and Dennis Boyer",
      "asked John Johnson, Jason Childress and Eleanor Ramirez",
      "See Martin Hall, Mathew Alvarez and Justin Davis",
      "Met with Ernestine Moore, Paul Hernandez and Alberta Morris",
      "met with Earl Ross, Julie Cervantes and Raul Kendrick",
      "meeting with Erin Williams, Thomas Lawson and Robert Hall",
      "for Jaime Payne, Jeffery Jennings and Henrietta Levy",
      "Met with Daphne Williams, Doris Cook and Amy Perez",
      "See Heather Fletcher, Christy Sherman and Brian Simmons",
      "Approved by Rosa Turner, Charles Warren and Melissa Finley",
      "see Betty Wade, Thelma Smith and Erin Dailey",
      "For Maria Brown, Hector Fontenot and Michael Mcgee",
      "For Sean Worley, Jason Johnson and Diane Willis",
      "lunch with Erica Harmon, Charlene Williams and Hilda Vaughn",
      "FOR Daniel Gagnon, John West and Bridget Garza",
      "See Bertha Cummings, Stacy Carroll and Marjorie Wilson",
      "meeting with Dave Cunningham, Wayne Hicks and Carrie Pace",
      "Approved by Sam Hayes, Anthony Mckenzie and Cheryl Hunt",
      "approved by Stanley Espinoza, Clay Riddle and Charles Warren",
      "approved Mildred Martin, Pierre Harper and Julie French",
      "approved Robin Zuniga, Robert Humphrey and Leslie Graves",
      "discussed with Paula Wells, Douglas Keith and Kelly Wilson",
      "approved by John Diaz, Gary Watkins and Debra Hunter",
      "Approved by Larry Duncan, Faye Franco and Frank Becker",
      "met with Sylvia Davis, Tracey Morrison and Marion Wilson",
      "meeting with Daniel Allison, Dolores Allen and Bruce Garcia",
      "customer meeting with Jerry Garcia, Margaret Nelson and Ralph Osborne",
      "Dorothea Ashley, Harvey Underwood and Rodney Mckee",
      "Approved by Cheri Cardenas, Christy Sherman and Elmer Stevens",
      "asked Teresa Evans, Stephen Jones and Peggy Davies",
      "approved by Don Miller, Brandon Slater and Joseph Rice",
      "See Jeremiah Parks, Fred Howell and Jackie Ritter",
      "asked Richard Williams, David Kelly and Georgia Mckinney",
      "ran by William Campbell, Tina Fontenot and Dana Vargas",
      "w/ Sean Patterson, Karen Munoz and Sharon Thompson",
      "discussed with Clifton Davis, Roy Burt and Ashley Willis",
      "approved by Sharon Farris, Pamela Johnson and Oscar Ford",
      "See Angela Blevins, Ruth Figueroa and Randy Moore",
      "approved by Sharon Mcdonald, Nancy Taylor and Terry Hansen",
      "APPROVED BY Antonio Smith, Judy Russell and Dorothea Ashley",
      "approved Heather White, Gerard Browning and Graciela Bullock",
      "Keith Campbell, William Taylor and Van Andrews",
      "Approved Jo Craig, Gerald Rogers and Leslie Gonzalez",
      "Ran by Elisabeth Marshall, Clara Rice and John Jones",
      "customer meeting with Stephanie Brown, Michelle Thomas and Jamie Wilcox",
      "with John Williams, Ruben Peters and Debbie Cox",
      "Approved by Rose Goodman, James Thompson and Bethany Bryant",
      "ran by Bobby Johnson, William Davis and Jeffrey Young",
      "Customer meeting with Sarah Bender, Sarah Guzman and Dennis Wright",
      "approved by April Jones, Jackie Spencer and Joan Schroeder",
      "Met with Dorothy Harrington, Juan Lee and Charles Hyde",
      "Stephen Sullivan, Wendy Pittman and Lester Hobbs",
      "See Lynn Wong, Stephen Miller and David Garrett",
      "See Robert Johnson, Darlene Petersen and Bruce Hughes",
      "For Thomas Baldwin, Amber Benson and Kelly Anderson",
      "discussed with Robert Miller, Sonya Alvarez and Billy Duncan",
      "Approved by Peggy Rivers, Heather Jones and Dennis Bright",
      "Approved by Donald Kelley, Cassandra Warner and Jason Berg",
      "meeting with Annette Edwards, Ivan Martin and Jennifer Ford",
      "See Janet Chavez, Marie Donovan and Joanne Jefferson",
      "see Jason Smith, Jeanette Curry and Sue Robertson",
      "Approved by Andrew Wilson, Michael Sanchez and Christina Russell",
      "asked Terri Turner, Margaret Berry and James Clemons",
      "w/ Jason Wilson, William Kennedy and Kenneth Figueroa",
      "CUSTOMER MEETING WITH Thomas Hunt, John Williams and Monica Williams",
      "met with John Wallace, Drew Turner and Stuart Morris",
      "discussed with Sandra Reeves, Kim Smith and Walter Mendoza",
      "ran by Andrew Moran, Dennis Strong and Ross Williams",
      "Met with William Stewart, Irene Mitchell and Nicole Cole",
      "For Steven Decker, Samuel Hicks and Shannon Cowan",
      "for James Lewis, Irene Mitchell and Leona Adams",
      "met with Tammy Smith, Mildred Perez and Damon Flynn",
      "customer meeting with Gayle Romero, Charles Potts and Teresa Dunn",
      "Asked Rochelle Hamilton, Sharon Rodriguez and Jeffery Jennings",
      "customer meeting with Julie Gallegos, Phillip Reed and Katie Adams",
      "See Sandra Dean, John Myers and Veronica Snow",
      "Approved by Tracey Nielsen, Tracy Cooper and Norma Armstrong",
      "meeting with Ronald Wilson, Sylvia Petersen and William Mcghee",
      "ran by Catherine Schultz, Barbara Evans and Roberta Pacheco",
      "asked Yvette Chavez, Brett Bryan and Mary Long",
      "approved by Oliver Sweet, Mary Lawson and Cassandra Hernandez",
      "customer meeting with Harold Mcneil, Jack Lowe and Willie Wallace",
      "approved June Bryant, Robert Hall and Robin Bryant",
      "approved Edgar Lopez, Steven Serrano and Gerald Rodriguez",
      "discussed with Mary Sharp, Amanda Conley and Lisa Lynch",
      "customer meeting with Joe Perry, Agnes Ellis and Carolyn Snider",
      "customer meeting with Robert Hawkins, Thomas Stone and Gregory Knowles",
      "asked Margaret Barnes, Meghan Sharp and Judy Fields",
      "Fay English, Susan Alvarez and Julie Williams",
      "WITH Bobby Cooper, Claire Hahn and Gerald Flores",
      "ran by Sheri Sims, Shelly Kim and Stacy Roy",
      "Margaret Jenkins, Lindsay Davis and Anna Roberts",
      "for Mary Small, James Lewis and Richard Knight",
      "meeting with Gary Smith, Roger Lewis and Tim Howell",
      "meeting with Sherry Ruiz, Crystal Green and Addie Kennedy",
      "asked Richard Collins, Stephanie Thornton and Lisa Hines",
      "Patrick Richardson, James Carter and Margaret Swanson",
      "See Franklin Horton, Joy Childs and Michael Gonzales",
      "asked Mike Jenkins, Johnathan Walker and Maria Martin",
      "for Daniel Allison, Vicki Ward and Robert Johnston",
      "lunch with Amy Hunt, Marjorie Wilson and Russell Murphy",
      "for James Barnes, Darlene Petersen and Daniel Novak",
      "Ran by Robert Jones, Kenneth Thornton and Walter Crawford",
      "Approved by Donald Kramer, Alan Andrews and Marshall Whitehead",
      "Ran by Marilyn Cox, Marvin Evans and Shelly Smith",
      "w/ Phillip Reed, Craig Griffin and Amy Ervin",
      "discussed with Cheri Cardenas, Mable Morris and Sarah Wilson",
      "Approved by Robert Johnson, Yolanda Ellison and Kathryn Burke",
      "lunch with Alicia Moran, Jeanette Martin and Joseph Gray",
      "approved Justin Hernandez, Ella Mendez and Ralph Mercer",
      "ran by Deborah Raymond, Marla Wood and Louise King",
      "with Jose Black, Clyde Bennett and Mary Brooks",
      "lunch with Logan Brewer, Lloyd Taylor and David Bentley",
      "see Colleen Williams, Carol Anderson and Michael Garcia",
      "LUNCH WITH Mary Brandon in McDonough",
      "approved Jennifer Skinner in Dudley",
      "meeting with Ronnie Willis in Fort McPherson",
      "see Louise Dunlap in Sirmans", "For Susan Hartman in Inaha",
      "See Tommy Perry in Villa Rica", "see Walter Baker in Glennville",
      "Discussed with Bruce Garcia in Nicholson",
      "approved Marcus Mathews in Cairo",
      "ran by Sherry Peterson in Tunnel Hill",
      "met with Kathryn Brown in Smyrna", "with Andrew Williams in Plains",
      "Eunice Grace in Baldwin", "for Mary Hernandez in Moniac",
      "w/ Iris Marshall in Oxford", "asked Elizabeth Perez in Mountain Park",
      "For Herbert Ramsey in Demorest",
      "Customer meeting with Erika White in Shiloh",
      "approved Louis Irwin in Lavonia", "approved Sonja Mcclure in Blythe",
      "for Kyle Howard in Norwood", "William Lopez in Alamo",
      "Latoya Norris in Warm Springs", "See James Cline in Mineral Bluff",
      "See Forrest Tanner in Gordon", "Van Hensley in Donalsonville",
      "with Michael Spencer in Canton", "Cecilia Williams in Stillwell",
      "Edward Hurley in Canon", "asked James Crowley in Rising Fawn",
      "met with Angela Neal in Sunsweet", "See David Ward in Comer",
      "asked Doris Schmidt in Hagan", "Thomas Dunbar in Shellman",
      "Approved by Bernard Brown in Forsyth",
      "Approved by Deborah Alford in Union City",
      "Approved by John Baker in Social Circle",
      "approved Nicole Ruiz in Sandersville",
      "approved Brittney Tucker in Trudie", "met with Amy Perez in Covington",
      "discussed with Aida Deleon in Fort Gordon",
      "see Bert Roberts in Dry Branch", "ran by Thelma Smith in Council",
      "See Carol Gonzales in Duluth", "Meeting with John Mcdowell in Payne",
      "w/ Rebecca Allen in Country Club Estate",
      "ran by Michael Delgado in Helen", "Mary Elliott in Bristol",
      "ran by Tracey Morrison in Ellaville",
      "Lunch with Elaine Hayes in Dixie", "ASKED Judy Smith in Woodbine",
      "Met with Cynthia Burns in Braswell",
      "lunch with Melvin Petersen in West Green",
      "See Anthony Melendez in Cornelia", "with Joseph Jensen in Blythe",
      "met with Richard Allen in Sargent", "Asked Albert Adams in Mount Berry",
      "approved Frederick Riddle in Meldrim",
      "approved Tracey Zimmerman in Bowdon", "w/ Theresa Chandler in Avera",
      "For Wesley Curtis in Warner Robins",
      "lunch with Andrea Wilson in Crescent",
      "Met with John Young in Hapeville", "Linda Rodriquez in Morven",
      "customer meeting with Jamie Wilcox in Dock Junction",
      "Approved by Donald Mitchell in Clayton",
      "See Raymond Molina in Georgetown", "For Roy Thompson in Abba",
      "meeting with Melissa Finley in Woodbine", "Amy Ervin in Holly Springs",
      "w/ Ryan England in Mystic", "w/ Dennis Bright in Plainville",
      "with Joyce White in McRae", "see Edwin Adams in Cartersville",
      "For Julie Smith in Lax", "SEE Paul Robinson in Vidette",
      "Steven Martinez in Thomasville", "approved Ruth Strickland in Kirkland",
      "asked Barry Miller in Tallapoosa",
      "Approved by Andrew Marsh in Harrietts Bluff",
      "for Tammy Jackson in Chatsworth", "w/ Sheila Reynolds in Cartersville",
      "For Phyllis Mitchell in Tifton", "asked Terry Ross in Yatesville",
      "customer meeting with Raul Martin in Folkston",
      "See Ralph Robinson in Helen", "For Lindsay Davis in Howard",
      "Kristine Miller in Cairo", "Discussed with Aaron Maloney in Avera",
      "meeting with Thomas Martin in Upton", "For Ola Flores in Hoschton",
      "asked Sterling Avery in Berkeley Lake",
      "discussed with Ursula Shepard in Summerville",
      "discussed with Michelle Henry in Thelma",
      "ran by Victoria Newton in Marietta",
      "Met with Melissa Young in East Dublin", "James Harris in Cogdell",
      "see Denise Smith in Bristol", "For Jason Moon in Augusta",
      "Meeting with Walter Hart in Dillard",
      "DISCUSSED WITH Stephanie Pena in Palmetto",
      "For Michael Beard in McCaysville", "See John West in Baconton",
      "asked Daniel Cox in Needmore", "Ran by Johnny Lawrence in Ailey",
      "See David Romero in Hogansville", "For Pat Martin in Tallapoosa",
      "met with Hubert Knight in Soperton",
      "customer meeting with Laurie Leblanc in Cornelia",
      "w/ Sue Robertson in Abba", "meeting with Ricky Maldonado in Nahunta",
      "Approved by Mayra Campos in Roopville",
      "approved Alfred Ruiz in Keller",
      "Approved Timothy Duke in Warner Robins",
      "customer meeting with Sandra Dean in Chula",
      "customer meeting with Kevin Davis in Fort Gaines",
      "Met with Alfredo Lopez in Kinderlou", "see Melissa Payne in Hampton",
      "With Jessica Watson in Calhoun", "Approved Gina Gonzalez in Temple",
      "Deborah Gentry in Colbert", "ran by Gregory Richardson in Meigs",
      "with Rachel Mckee in Edge Hill", "approved Donald Freeman in Chamblee",
      "asked Katherine Wilder in Atlanta",
      "DISCUSSED WITH Catherine Olson in Waynesville",
      "See Stacy Starr in Crawfordville",
      "Ran by David Cooper in Sandersville", "ran by Rodney Meyer in Scotland",
      "for Kelly Fuentes in Bremen", "w/ Ann Smith in Washington",
      "Tiffany Ryan in Unadilla", "See Ben Fields in Sirmans",
      "Approved by Patricia Hanna in Dahlonega",
      "asked Eddie Brown in Conyers", "approved by Joseph Francis in Crawford",
      "asked Betty Vazquez in Rebecca",
      "Approved by Dorothy Miller in Brooklet", "with Mary Rice in McDonough",
      "approved Gary Watkins in Lawrenceville",
      "Approved by Mary Bruce in Payne",
      "customer meeting with Brian Spencer in Vidette",
      "Met with Grover Adams in Chamblee",
      "Customer meeting with Ramon Velasquez in Fairburn",
      "met with Amy Burks in Bowens Mill", "with David Wagner in Glenwood",
      "customer meeting with Michael Snider in Council",
      "customer meeting with Elias Lewis in East Dublin",
      "customer meeting with Adele Hall in Sparta",
      "See Tara Wright in Sandersville", "RAN BY Sheena Gray in Ebenezer",
      "ran by Ruben Peters in Hopeulikit",
      "met with Harold Santiago in Thomasville",
      "Asked Kate Preston in Helena", "ran by Gloria Boggs in Harlem",
      "approved by Thomas Bowers in Fort Gaines",
      "w/ Paul Escobar in Midville",
      "customer meeting with Adam Noble in Stockton",
      "Approved Inez Acosta in Milledgeville",
      "for Manuel Forrest in Buchanan", "with Cheryl King in Eton",
      "Kenneth Barker in Harding", "see Carol Anderson in Lenox",
      "For Jasper Hernandez in Dalton", "Alice Nance in Luthersville",
      "lunch with David Cooper in Tate", "For Mercedes Cannon in Morrow",
      "asked Jason Davis in Glennville",
      "approved Gabrielle Johnson in Nankin",
      "Approved Gregg Williams in Cartersville", "w/ Carroll Daniel in Buford",
      "For Peggy Shelton in Sunbury", "asked David Cooper in Sharon",
      "with Lillian Wilson in Barney", "Approved by Jimmie Dunn in Oxford",
      "CUSTOMER MEETING WITH James Daniel in Daisy",
      "for John Smith in Vidette", "discussed with Edward Nixon in Irwinville",
      "customer meeting with Suzanne Owens in Statham",
      "w/ George Benson in Louisville", "For Monica Kent in Kingston",
      "ran by Yvette Faulkner in Axson", "See James Holt in Chatsworth",
      "Discussed with John Robinson in Lakeland",
      "For Clarence Macias in Fortson", "For Viola Roy in Shiloh",
      "met with Sidney Walker in Daisy",
      "approved Ernest Thornton in Luthersville",
      "For Irene Lawrence in Oxford", "Approved by Mary King in Athens Clarke",
      "with Paul Phillips in Clyo",
      "for Emmanuel Morales in Saint Simons Island",
      "lunch with Shirley Brown in Montgomery", "w/ Lisa Hines in Ambrose",
      "w/ Ronald Beard in Chickamauga", "Ran by James Sloan in Nicholson",
      "For Keith Mann in Eastanollee", "lunch with Rose Powers in Ellabell",
      "met with Patricia Kennedy in Hamilton", "asked Sammy Johnson in Poulan",
      "lithonia", "Denton", "Bellville", "LaGrange", "Dahlonega", "Lake Park",
      "Palmetto", "Lawrenceville", "Jeffersonville", "Dover Bluff", "Cataula",
      "CHULA", "Clarkston", "andersonville", "Stephens", "Adrian", "Martinez",
      "Cobb", "Swainsboro", "Lulaton", "Manchester", "Sargent", "JONESBORO",
      "Valona", "Thomaston", "Sylvania", "bronwood", "Columbus", "Barretts",
      "Dalton", "Vidette", "Luthersville", "Meigs", "NEW ROCK HILL",
      "Johns Creek", "West Point", "Colesburg", "Eldorado", "Kinderlou",
      "jeffersonville", "Mount Zion", "Nicholls", "Thomasville", "Davisboro",
      "MONROE", "La Fayette", "Folkston", "Greenville", "Ball Ground",
      "Thomaston", "Albany", "Greensboro", "richland", "Milledgeville",
      "Townsend", "NEWTON", "Cusseta", "Nankin", "Savannah", "West Point",
      "Danielsville", "Cornelia", "Palmetto", "Thomson", "Ellerslie",
      "adairsville", "THOMASVILLE", "Colquitt", "Norman Park", "McRae",
      "Omaha", "Meansville", "Dixie", "Pembroke", "Eastman", "Tate", "Pearson",
      "FLOWERY BRANCH", "comer", "Jekyll Island", "Sylvester", "Colesburg",
      "Molena", "Georgetown", "Saint George", "Decatur", "Baconton",
      "Culloden", "IRWINTON", "Fort Gaines", "Aragon", "royston", "Swainsboro",
      "Arlington", "Ila", "Payne", "Stockton", "Ousley", "Statenville",
      "BOWMAN", "Inaha", "Sunny Side", "Tallapoosa", "Resaca", "waynesville",
      "Screven", "Fort Oglethorpe", "Cobbtown", "Pitts", "Atlanta", "WALESKA",
      "Mershon", "Riceboro", "Eastman", "Wray", "Oliver", "Oglethorpe",
      "berkeley lake", "Austell Douglas", "Sandy Springs", "Canton",
      "JEFFERSON", "Santa Claus", "Crawfordville", "Dallas", "Douglasville",
      "Cusseta", "Manchester", "Albany", "Vidalia", "dalton", "West Green",
      "MANCHESTER", "Warm Springs", "Dawson", "Cordele", "Pitts", "Waterloo",
      "Winder", "Lawrenceville", "Grantville", "McRae", "Hopeulikit", "leary",
      "Cleveland", "Demorest", "Winterville", "Grantville", "Crawfordville",
      "Lakemont", "Leary", "Luthersville", "Elberton", "Fort Stewart",
      "MORGAN", "Lyons", "arnoldsville", "Dunwoody", "Avondale Estates",
      "Edge Hill", "Mystic", "Nevils", "Rockmart", "Fort Gaines", "Roopville",
      "HAMILTON", "Withers", "Warm Springs", "Grooverville", "brunswick",
      "Ray City", "Bostwick", "Winterville", "Waynesboro", "Temple",
      "Powder Springs", "BLUE RIDGE", "Daisy", "Preston", "Brunswick",
      "Lumpkin", "Coleman", "statesboro", "Springfield", "Nankin", "Denton",
      "Appling", "ACWORTH", "Wilmington Island", "Cuthbert", "Mount Zion",
      "Mount Berry", "Rockingham", "Cave Spring", "Fort Oglethorpe", "everitt",
      "Tarver", "Fort Stewart", "AMBOY", "Buena Vista", "MEETING WITH Gary",
      "for Joseph", "Lunch with Felix", "for John", "for Barbara", "asked Jon",
      "w/ Joyce", "lunch with Carol", "discussed with Scott",
      "discussed with Steven", "John", "ran by Carlos", "for Micheal",
      "For Nancy", "For Anthony", "Hugo", "See Douglas", "for Wanda",
      "See Jennifer", "see Anthony", "customer meeting with Stephanie",
      "ran by Randy", "With Terrance", "customer meeting with Linda",
      "customer meeting with David", "FOR william", "Asked Elliot",
      "discussed with Fred", "ran by Susan", "lunch with donald",
      "customer meeting with Loretta", "ran by Jay", "see Ronald",
      "lunch with Leo", "discussed with Luis", "Approved Margaret",
      "with Herman", "For Larry", "lunch with Eleanor", "approved by David",
      "See Marta", "See Randolph", "See Michael", "asked Kathleen",
      "with Patricia", "Edward", "For Robert", "customer meeting with Matthew",
      "see Charles", "met with Debora", "DISCUSSED WITH June",
      "meeting with Chris", "approved by edward", "See Clayton", "Paula",
      "w/ Clifton", "Approved by Bobby", "with Randall", "With Paul",
      "approved Nicole", "customer meeting with Hannah", "meeting with Brenda",
      "met with zachary", "approved Allan", "For Matthew", "asked Joseph",
      "see Yvonne", "asked Alexander", "For Timothy", "For Peter",
      "Approved by Joey", "See Anna", "asked Theodore", "with Marilyn",
      "Approved by Diana", "MET WITH Juan", "John", "See Shirley",
      "approved by Mary", "meeting with Joyce", "w/ Michael", "For amanda",
      "Clifford", "approved Hugh", "approved by William", "for Mark",
      "customer meeting with Julie", "for Katie", "W/ logan",
      "Approved by Arlene", "w/ Eileen", "approved Gina", "See James",
      "For Joyce", "see Kevin", "w/ Agnes", "Meeting with Joseph",
      "For Michael", "Meeting with Anthony", "See Annie",
      "LUNCH WITH James & Jeffrey", "Christopher & Jason",
      "see Vickie & Pamela", "approved by Fannie & Violet",
      "discussed with christina & Jean", "Gary & Percy",
      "customer meeting with Patrick & Eddie", "w/ Joseph & Emily",
      "W/ Michael & Carol", "discussed with Richard & Ginger",
      "Approved by Chad & barbara", "Greg & Michael", "see Thomas & George",
      "Approved by Sandra & Louis", "See Paul & Randy",
      "asked Sherry & Sherri", "meeting with Misty & Kendrick",
      "meeting with Phillip & Gilbert", "Meeting with Curtis & matthew",
      "For Richard & Jerome", "customer meeting with Carla & Natasha",
      "ran by Alice & Stella", "Christopher & Salvador",
      "Lunch with Joe & George", "customer meeting with Robert & Andrew",
      "CUSTOMER MEETING WITH Tracy & john", "Ran by Kenneth & Jamie",
      "See Dexter & Elizabeth", "w/ Sara & Minnie", "For Todd & Kellie",
      "approved by Rita & Jeremy", "W/ Christina & David",
      "Meeting with Mathew & Cheryl", "For Brittney & Samuel",
      "Approved Judith & Byron", "approved Carmen & Alma",
      "Approved by Melvin & Zachary", "With Brandon & Lisa",
      "w/ Gregory & Gloria", "W/ Autumn & Charles", "with Donald & Susan",
      "Approved by Florence & Nancy", "meeting with James & William",
      "Meeting with Eileen & Melvin", "see Stuart & Matthew",
      "approved by Anthony & Jeffrey", "w/ Dorothy & Lori",
      "for Amanda & Tiffany", "for Harry & Mildred", "for Billy & Angel",
      "FOR Randi & Rodney", "approved by Lance & Ricky", "see debra & Jon",
      "Approved by Joseph & Sammy", "meeting with Andrew & Consuelo",
      "see david & Cynthia", "Approved by Kelvin & Sherri",
      "see Francis & Aubrey", "for Anne & Paul",
      "discussed with Sandra & Alexander", "lunch with Raymond & Mary",
      "See Sally & William", "ran by elton & David", "For Luis & William",
      "approved Amanda & Debbie", "Approved william & Alex",
      "Discussed with Kari & Debbie", "meeting with Ralph & Carrie",
      "w/ Jeffrey & leland", "asked Kathryn & Jeremy",
      "Approved by Richard & Denise", "Approved by Jason & Connie",
      "for Everett & James", "See John & Robyn",
      "discussed with Earnest & Michael", "MET WITH Bob & James",
      "met with Max & Jennifer", "w/ Paul & Robert", "For Christine & Robert",
      "Asked Richard & Jonathan", "with Geraldine & Mary",
      "asked Amy & Benjamin", "Customer meeting with Julie & Toni",
      "lunch with Dixie & Mark", "approved by David & Louis",
      "ran by Rudy & Jaime", "Jennifer & michael", "for Joseph & Patty",
      "With Stephen & Jacob", "lunch with Edward & Brenda",
      "Approved by Susan & Doris", "approved George & Carlos",
      "Lunch with John & Nancy", "customer meeting with Tonya & Aaron",
      "ran by Martin & Charles", "Lunch with Tracey & Robert",
      "see Anthony & Jennifer", "w/ Kimberly & Angela", "See Freddy & Danny",
      "Approved Jim & Casey", "Jermaine Taylor approved.",
      "Sandra Jefferson Approved", "Paul Phillips approved.",
      "Chad Martinez approved.", "Bryan White lunch", "Julie Smith approved",
      "Juan Thompson meeting", "Larry Cruz meeting", "Catherine Cox lunch",
      "Jeffery Merritt meeting", "Teresa Evans approved.",
      "Larry Taylor meeting", "Mable Morris approved", "Mark Bell Approved",
      "Theresa Dunlap Approved", "Courtney Glover meeting",
      "James Johnson lunch", "Jose Harrison approved.",
      "Salvador Becker Approved", "Brian Jordan lunch", "Margaret Floyd lunch",
      "Albert Dodson lunch", "Heather Price meeting", "Eugene Downing meal.",
      "Teresa Thomas approved.", "Andrew Davis lunch", "Kirk Clark Approved",
      "Daniel Manning approved.", "Judy Woods lunch", "Aaron Strong lunch",
      "Laura Farley lunch", "Michael Guzman approved.",
      "Cynthia Roach approved.", "Miguel Dixon meal.", "Walter Hicks lunch",
      "Dennis Strong Approved", "Ashley Ellison approved.",
      "Owen Lindsey approved", "Melissa French approved",
      "Christopher Frazier meal.", "Emily Sykes approved",
      "Christopher Thompson meal.", "Tammy Greer lunch", "Connie Allen lunch",
      "Steve Curry lunch", "Dolores Jones approved", "Justin Hill lunch",
      "Gregg Jackson Approved", "Hazel Smith approved.", "James Brown lunch",
      "Sophie Conner approved.", "Stanley Espinoza meal.",
      "Randall Jones Approved", "Richard Mckinney meeting", "Bill Cook meal.",
      "John Guerrero meal.", "Linda Graves Approved", "Sara Cruz meal.",
      "Charles Manning meeting", "Lola Newman meal.", "Lucille Henson meal.",
      "George Anthony meal.", "Craig Griffin approved.",
      "Michael Barber lunch", "Cindy Williams lunch",
      "Christine Alvarado lunch", "Genevieve Contreras meal.",
      "Barbara Washington approved.", "Lonnie Jackson approved",
      "Connie Allen meal.", "Alicia Osborne approved", "Janet Smith meeting",
      "Joan Powell approved.", "Clarice Garcia approved",
      "Deborah Grace lunch", "Luis Carpenter meeting", "Gary Horton meal.",
      "Judith Barker approved.", "Gladys Lopez lunch", "Stephen Rivera meal.",
      "Tony West approved.", "Betty Bowers approved.",
      "Allison Torres approved", "Thomas Miller approved",
      "Carlos Gray meeting", "Charles Potts lunch", "Pedro Parsons approved.",
      "Andrew Kelly lunch", "Ana Clayton Approved", "Elizabeth Costa meeting",
      "Omar Page meeting", "Annette Edwards approved",
      "Herbert Ramsey meeting", "Carol Albert meal.",
      "Jerry Edwards approved.", "Robert Russell approved",
      "Michael Davis approved.", "Christine Gross meal.",
      "Tiffany Keller approved.", "Margaret Taylor meeting",
      "Maxine Murray meal.", "Ed Davies Approved", "Sherry Davis meeting",
      "Jackie Spencer approved", "Philip Nelson meal.",
      "Judy Sanchez approved.", "Ray Turner meal.", "Norma Thompson Approved",
      "Javier Noel meal.", "William Nelson meal.", "James Hendricks lunch",
      "Joyce Reed meal.", "Terri Long meeting", "Evelyn Bryant approved",
      "John Warren Approved", "Patricia Waters approved",
      "Edward Nguyen approved.", "Dorothy Garcia meal.",
      "Michael Simpson Approved", "Darrel Jenkins approved.",
      "Patrick Bowen Approved", "Melvin Martinez lunch", "Raymond Jones lunch",
      "Margaret Taylor meeting", "Gregory Wright approved",
      "Edith Soto Approved", "David Moran approved", "James Burns approved.",
      "Martin Weber lunch", "Henry Hall lunch", "Judith Clements approved",
      "Janice Burke meeting", "Betty Morgan approved.",
      "Kristin Meadows approved.", "Mandy Warner meeting",
      "Norma Morgan approved.", "Steven Martinez approved",
      "Hector Cross lunch", "John Johnson meal.", "Carl Clay Approved",
      "Clyde Robertson Approved", "Ida Jackson Approved", "James Guy approved",
      "Shelly Ryan approved.", "Joey Arrington meal.", "Patrick Parks lunch",
      "Brian Edwards lunch", "John Bailey meeting", "Donald Mcdonald meeting",
      "Shawn Quinn approved", "Brent Howard lunch", "Garrett Ellis approved",
      "Roberto George lunch", "Sybil Cook approved.", "Lynn Edwards approved",
      "Crystal Campbell lunch", "Lindsay Davis lunch",
      "Jasmine Rogers approved", "Denise Smith approved.",
      "Larry Wilson Approved", "Dewey Delgado Approved",
      "Patrick Shirley Approved", "Joseph Taylor meeting",
      "Francisco Smith meal.", "Johnathan Walker Approved",
      "Archie Austin lunch", "Sharon Thompson approved.",
      "John Mathis meeting", "Jerry Vinson lunch", "Jessie Clark meal.",
      "Sean Finley Approved", "Wilson Howard approved", "William Hodges lunch",
      "Teresa Glover meal.", "Katherine Barton approved",
      "Dina Johnson approved.", "John Lee meal.", "Judy Woodward approved.",
      "Christopher Vega meeting", "Joseph Johnson approved.",
      "Janet Pitts approved.", "Melvin Richardson approved",
      "Vickie Jacobs approved", "Micheal Collier lunch",
      "Shirley Wallace approved.", "Raymond Gonzalez lunch",
      "Luis Atkins approved", "Francisca Rivera approved", "Gary Jones lunch",
      "Thomas Oliver approved.", "Russell Bush approved",
      "Laura Stokes meeting", "Cynthia Rodgers approved",
      "Richard Christensen meeting", "Ruben York Approved",
      "Craig Wilson Approved", "Steven Mitchell meal.",
      "Brittney Grant approved", "Samantha Petersen meal.",
      "Oliver Perkins approved", "Janice meeting", "Tony approved",
      "Debra meal.", "Jennifer meal.", "Lorraine approved", "Tammy approved.",
      "Patricia Approved", "George meeting", "Eric approved.",
      "Walter Approved", "Ricky approved.", "Olga lunch", "Autumn approved",
      "Frank meeting", "Graciela Approved", "Ronald meeting", "Leah approved",
      "Kelly approved.", "Robert approved", "Judith meeting",
      "Kimberly approved.", "Margaret lunch", "Dexter lunch", "Gina lunch",
      "Susan Approved", "Frances meal.", "Kathleen meal.", "William meeting",
      "Ann approved.", "Martha approved", "Joseph approved", "Charity meeting",
      "Angelica Approved", "Mary meal.", "Linda meeting", "jessica lunch",
      "Ronald meal.", "Brian approved.", "Harvey approved", "Susan approved.",
      "Florence lunch", "Linda approved.", "Ivy approved", "Julia approved",
      "Terrance meeting", "Jason approved", "James approved.",
      "Michele Approved", "Darlene meeting", "Wade meal.", "William meal.",
      "Shannon Approved", "Doris approved.", "Jason meal.", "Jeanne approved",
      "Robert lunch", "Lillian lunch", "Linda Approved", "Jose meal.",
      "Jesus lunch", "Howard Approved", "Ann approved", "John lunch",
      "cecile lunch", "Rodney meeting", "James lunch", "Stacey meal.",
      "Deborah lunch", "Roy Approved", "Lindsay approved", "Tom lunch",
      "Katherine approved.", "Clyde Approved", "Jose approved.",
      "Patricia meal.", "Douglas meal.", "Felipe meal.", "Herbert meal.",
      "Randy meeting", "Norma lunch", "Herbert approved", "Dorothy Approved",
      "Wanda meal.", "Tammy approved", "Bill approved", "Richard approved",
      "Ellen approved", "Eileen meal.", "Mary Approved", "Toni meal.",
      "Richard meal.", "Bryan approved", "Roger lunch", "Alma meal.",
      "Charles meeting", "Pam lunch", "Edward Approved", "Jose Approved",
      "James meeting", "Stella approved.", "Louisville meal.",
      "Stone Mountain Approved", "Stapleton lunch", "Suwanee approved.",
      "Harlem Approved", "Hull approved.", "Winder meal.",
      "Twin City approved.", "New Rock Hill lunch", "Lenox approved",
      "Nankin approved.", "Swainsboro approved", "Doerun approved",
      "Council approved.", "Pridgen Approved", "Mineral Bluff approved",
      "Lyons approved.", "Madison meal.", "Weber meal.", "Ray City approved.",
      "Wildwood Approved", "Pinehurst approved.", "Mount Vernon lunch",
      "Riceboro lunch", "Cochran meeting", "Forest Park meal.",
      "Dahlonega meeting", "Resaca meal.", "Camilla meal.", "Emerson Approved",
      "Withers approved.", "Colquitt Approved", "Shellman Bluff approved.",
      "McRae approved", "Waleska Approved", "Roswell meal.",
      "Clarkston approved", "Amboy Approved", "Crawfordville meeting",
      "Buchanan approved.", "Fort Stewart lunch", "Yatesville lunch",
      "Woodland meeting", "Decatur lunch", "Commerce Approved",
      "Flemington meal.", "Nicholls meeting", "Thomson approved.",
      "Lovejoy lunch", "Demorest approved.", "Mount Berry lunch",
      "Riverdale approved", "Wrightsville meeting", "Mountain Park approved.",
      "Vidette Approved", "Willacoochee approved", "Cobbtown Approved",
      "Dillard Approved", "Willacoochee Approved", "Skidaway Island Approved",
      "Greenville lunch", "Dover Bluff meal.", "Mountain Park meeting",
      "Hagan meeting", "Georgetown Approved", "Roberta approved",
      "Dock Junction approved.", "Jeffersonville approved.", "Pooler meeting",
      "Chatsworth approved.", "Jonesboro lunch", "Whitemarsh Island approved",
      "Stillwell meal.", "Fairmount meal.", "Pridgen meal.", "Avera Approved",
      "Santa Claus approved", "Vidette lunch", "Remerton lunch",
      "Bowman approved", "Cedar Springs lunch", "Nankin approved",
      "Willacoochee approved", "Locust Grove meeting", "Waleska approved.",
      "Norcross approved.", "Byron meal.", "Luthersville meeting",
      "Carrollton meeting", "Barretts lunch", "Fortson Approved",
      "Dawsonville meal.", "Homeland Approved", "Emerson meal.",
      "Brunswick lunch", "Mineral Bluff meal.", "Broxton Approved",
      "Patterson lunch", "Lincolnton approved.", "Decatur approved.", "broken",
      "BROKEN", "Broken", "conference", "CONFERENCE", "Conference",
      "replacement", "REPLACEMENT", "Replacement", "maintenance",
      "MAINTENANCE", "Maintenance", "lost", "LOST", "Lost", "customer meeting",
      "CUSTOMER MEETING", "Customer meeting", "sales opportunity",
      "SALES OPPORTUNITY", "Sales opportunity", "emergency", "EMERGENCY",
      "Emergency", "backup", "BACKUP", "Backup", "research", "RESEARCH",
      "Research", "Broken, conference", "Conference, replacement",
      "Replacement, maintenance", "Maintenance, lost",
      "Lost, customer meeting", "Customer meeting, sales opportunity",
      "Sales opportunity, emergency", "Emergency, backup", "Backup, research"};

  private static final double SUNDRY = 100;

  public static DataGenerationServiceImpl getInstance() {
    return instance;
  }

  private long endTime;
  
  // List<Object> toPersist = new ArrayList<Object>();
  
  private Random rand = new Random();
  
  public void delete() {
    for (Employee e : Employee.findAllEmployees()) {
      e.remove();
    }
    for (Report r : Report.findAllReports()) {
      r.remove();
    }
    for (Expense e : Expense.findAllExpenses()) {
      e.remove();
    }
  }

  public int generate(int millis) {
    long startTime = System.currentTimeMillis();
    endTime = startTime + millis;
//    log.info("In generate(" + millis + ") at " + startTime + " (left = " + (endTime - startTime) + ")");
    int startEmployees = (int) Employee.countEmployees();
    int startReports = (int) Report.countReports();
    int numEmployees;
    
    synchronized (DataGenerationServiceImpl.class) {
//      long start, end;
      try {
        if (lastNames.size() == 0) {
//          start = System.currentTimeMillis();
          readFile("dist.all.last.txt", lastNames, lastNameFreqs);
//          end = System.currentTimeMillis();
//          log.info("Read dist.all.last.txt in " + (end - start) + " milliseconds");
        }
        if (femaleNames.size() == 0) {
//          start = System.currentTimeMillis();
          readFile("dist.female.first.txt", femaleNames, femaleNameFreqs);
//          end = System.currentTimeMillis();
//          log.info("Read dist.female.first.txt in " + (end - start) + " milliseconds");
        }
        if (maleNames.size() == 0) {
//          start = System.currentTimeMillis();
          readFile("dist.male.first.txt", maleNames, maleNameFreqs);
//          end = System.currentTimeMillis();
//          log.info("Read dist.male.first.txt in " + (end - start) + " milliseconds");
        }
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }
    }

    // Choose department and make a manager
    int department = rand.nextInt(DEPARTMENTS.length);
//    long supervisorId = makeEmployee(department, 0, false);
//    if (supervisorId == -1) {
//      return startReports;
//    }
    long supervisorId = 1;
    
    long time;
    while ((time = System.currentTimeMillis()) < endTime) {
//      log.info("Calling makeEmployee from generate at " + time + " (left = " + (endTime - time) + ")");
      makeEmployee(department, supervisorId, true);
    }
    
    numEmployees = (int) Employee.countEmployees();
    int numReports = (int) Report.countReports();
    int reportsCreated = numReports - startReports;
    int employeesCreated = numEmployees - startEmployees;
    log.info("Generated " + employeesCreated + " employees and " +
        reportsCreated + " reports in " +
        (System.currentTimeMillis() - startTime) + " milliseconds");
    
    return numReports;
  }

  public int getNumReports() {
    synchronized (DataGenerationServiceImpl.class) {
      return (int) Report.countReports();
    }
  }

  private double amount(double max) {
    double x = (1.0 + rand.nextDouble()) * max * 0.5;
    x *= 100;
    x = Math.floor(x);
    x /= 100;
    return x;
  }

  private String chooseName(List<String> names, List<Double> freqs) {
    double lastFreq = freqs.get(freqs.size() - 1);
    double freq = rand.nextDouble() * lastFreq;

    int index = Collections.binarySearch(freqs, freq);
    if (index < 0) {
      index = -index - 1;
    }
    String name = names.get(index);
    return name;
  }

  private long makeEmployee(int department, long supervisorId, boolean makeExpenses) {
    long time = System.currentTimeMillis();
    if (time >= endTime) {
//      log.info("Returning from makeEmployee at " + time + " (left = " + (endTime - time) + ")");
      return -1;
    }
    
    String firstName;
    String lastName = chooseName(lastNames, lastNameFreqs);
    if (rand.nextInt(2) == 0) {
      firstName = chooseName(femaleNames, femaleNameFreqs);
      if (rand.nextDouble() < FEMALE_HYPHENATE) {
        lastName += "-" + chooseName(lastNames, lastNameFreqs);
      }
    } else {
      firstName = chooseName(maleNames, maleNameFreqs);
      if (rand.nextDouble() < MALE_HYPHENATE) {
        lastName += "-" + chooseName(lastNames, lastNameFreqs);
      }
    }

    Employee employee = new Employee();
    employee.setSupervisorKey(supervisorId);
    employee.setUserName(userName(firstName, lastName));
    employee.setDisplayName(firstName + " " + lastName);
    employee.setDepartment(DEPARTMENTS[department]);
    employee.setPassword("");
    
//    long start = System.currentTimeMillis();
    employee.persist();
//    log.info("Persisted employee in " + (System.currentTimeMillis() - start) + " millis");

    long id = employee.getId();
    String dept = employee.getDepartment();
    
    if (makeExpenses) {
      int numExpenseReports = rand.nextInt(96) + 5;
      for (int i = 0; i < numExpenseReports; i++) {
        if (!makeExpenseReport(id, supervisorId, dept)) {
          return id;
        }
      }
    }
    
    return id;
  }

  private void makeExpenseDetail(long reportId, Date date, String category,
      String description, double amount) { 
    Expense expense = new Expense();
    expense.setReportId(reportId);
    expense.setDescription(description);
    expense.setCreated(date);
    expense.setAmount(amount);
    expense.setCategory(category);
    expense.setApproval("");
    expense.setReasonDenied("");
    
//    long start = System.currentTimeMillis();
    expense.persist();
//    log.info("Persisted expense in " + (System.currentTimeMillis() - start) + " millis");
  }

  private boolean makeExpenseReport(long employeeId, long supervisorId,
      String department) {
    long time = System.currentTimeMillis();
    if (time >= endTime) {
//      log.info("Returning from makeExpenseReport at " + time + " (left = " + (endTime - time) + ")");
      return false;
    }
    
    long offset = rand.nextInt(60 * 60 * 24 * 90) * 1000L;
    long millis = new Date().getTime() - offset;

    Date createdDate = new Date(millis);

    Report report = new Report();
    report.setReporterKey(employeeId);
    report.setDepartment(department);
    report.setApprovedSupervisorKey(supervisorId);
    report.setCreated(createdDate);
    boolean travel = rand.nextInt(4) == 0;
    int days = 1;
    
    if (travel) {
      days = rand.nextInt(10) + 1;
      int index1 = rand.nextInt(CITIES.length);
      int index2 = index1;
      while (index2 == index1) {
        index2 = rand.nextInt(CITIES.length);
      }

      report.setPurpose("Travel from " + CITIES[index1] + " to "
          + CITIES[index2]);
      report.setNotes("Travel for " + days + " days");
    } else {
      report.setPurpose(PURPOSES[rand.nextInt(PURPOSES.length)]);
      report.setNotes(NOTES[rand.nextInt(NOTES.length)]);
    }

//    long start = System.currentTimeMillis();
    report.persist();
//    log.info("Persisted report in " + (System.currentTimeMillis() - start) + " millis");
    
    long id = report.getId();
    
    if (travel) {
      days = rand.nextInt(10) + 1;
      int index1 = rand.nextInt(CITIES.length);
      int index2 = index1;
      while (index2 == index1) {
        index2 = rand.nextInt(CITIES.length);
      }

      makeExpenseDetail(id, new Date(millis - days * MILLIS_PER_DAY),
          "Air Travel", "Outbound flight", amount(AIRFARE));
      makeExpenseDetail(id, new Date(millis - MILLIS_PER_DAY / 2),
          "Air Travel", "Return flight", amount(AIRFARE));
      for (int i = 0; i < days; i++) {
        makeExpenseDetail(id, new Date(millis - (days - i) * MILLIS_PER_DAY
            - 10 * MILLIS_PER_HOUR), "Dining", "Breakfast", amount(BREAKFAST));
        makeExpenseDetail(id, new Date(millis - (days - i) * MILLIS_PER_DAY - 6
            * MILLIS_PER_HOUR), "Dining", "Lunch", amount(LUNCH));
        makeExpenseDetail(id, new Date(millis - (days - i) * MILLIS_PER_DAY - 2
            * MILLIS_PER_HOUR), "Dining", "Dinner", amount(DINNER));
        makeExpenseDetail(id, new Date(millis - (days - i) * MILLIS_PER_DAY),
            "Lodging", "Hotel", amount(HOTEL));
      }
    } else {
      int numExpenses = rand.nextInt(5) + 1;
      for (int i = 0; i < numExpenses; i++) {
        int index = rand.nextInt(CATEGORIES.length);
        long detailOffset = rand.nextInt(60 * 60 * 24 * days) * 1000L;
        Date date = new Date(createdDate.getTime() - detailOffset);
        makeExpenseDetail(id, date, CATEGORIES[index], DESCRIPTIONS[index],
            amount(SUNDRY));
      }
    }
    
//    log.info("Made report in " + (System.currentTimeMillis() - time) + " millis");
    return true;
  }

  private void readFile(String filename, List<String> names,
      List<Double> frequencies) throws IOException {
    Reader reader = new FileReader(filename);
    BufferedReader br = new BufferedReader(reader);

    names.clear();
    frequencies.clear();

    String s;
    while ((s = br.readLine()) != null) {
      String[] split = s.split("\\s+");
      String name = split[0];
      if (name.startsWith("MC") && name.length() > 2) {
        name = "Mc" + name.charAt(2) + name.substring(3).toLowerCase();
      } else {
        name = "" + name.charAt(0) + name.substring(1).toLowerCase();
      }
      names.add(name);
      frequencies.add(Double.parseDouble(split[2]));
    }

    // Disambiguate names with equal cumulative frequencies
    double lastFreq = 0;
    int count = 1;
    int len = frequencies.size();
    for (int i = 0; i < len; i++) {
      Double freq = frequencies.get(i);
      if (freq == lastFreq) {
        count++;
        continue;
      } else {
        if (count > 1) {
          for (int c = 0; c < count; c++) {
            double newFreq = lastFreq + (.001 * c) / count;
            frequencies.set(i - count + c, newFreq);
          }
          count = 1;
        }

        lastFreq = freq;
      }
    }
  }

  private String userName(String firstName, String lastName) {
    return ("" + firstName.charAt(0) + lastName + rand.nextInt(100)).toLowerCase();
  }
}
