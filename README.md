# Store Management System

A Java-based store management system that handles product inventory, sales, and cashier management.

## Features

- Product management with expiration dates
- Different markup percentages for food and non-food items
- Expiration-based price discounts
- Cashier management and register assignments
- Receipt generation with all required information
- File saving for receipts (both text and serialized format)
- Total revenue and receipt tracking
- Serialization and deserialization of receipts
- Store financial calculations (expenses, income, profit)
- Exception handling and comprehensive testing

## Requirements

- Java 11 or higher
- Gradle 8.5 or higher (included via Gradle Wrapper)

## Building the Project

To build the project, run:

```bash
./gradlew build
```

This will:
1. Compile the source code
2. Run the tests
3. Create a JAR file in the `build/libs` directory

## Running the Application

After building, you can run the application using:

```bash
./gradlew run
```

Or run the JAR directly:

```bash
java -jar build/libs/store-management-1.0-SNAPSHOT.jar
```

## Running Tests

To run the tests:

```bash
./gradlew test
```

## Other Useful Gradle Commands

- `./gradlew clean` - Clean the build directory
- `./gradlew jar` - Create JAR file only
- `./gradlew check` - Run all checks (tests, static analysis)
- `./gradlew tasks` - Show all available tasks

## Project Structure

- `src/main/java/` - Main source code
  - `Product.java` - Product class with expiration date management
  - `ProductCategory.java` - Product category enum (FOOD/NON_FOOD)
  - `Cashier.java` - Cashier class with register assignment
  - `Store.java` - Main store management class
  - `Receipt.java` - Receipt generation and serialization
  - `Main.java` - Application entry point
- `src/test/java/` - Test source code
  - `StoreTest.java` - Test cases for the store system
- `build.gradle` - Gradle build configuration
- `gradle/` - Gradle wrapper files
- `gradlew` / `gradlew.bat` - Gradle wrapper scripts

## Features in Detail

### Receipt Management
- Receipts are saved in two formats:
  - Text files (`receipt_N.txt`) for human-readable format
  - Serialized files (`receipt_N.ser`) for object persistence
- Receipts can be deserialized and read from files
- Each receipt contains:
  - Unique receipt number
  - Date and time
  - Cashier information
  - List of purchased items
  - Total amount

### Store Financial Management
- Tracks:
  - Delivered products
  - Sold products
  - Cashiers and their assignments
  - Issued receipts
- Calculates:
  - Salary expenses
  - Delivery expenses
  - Total income
  - Store profit

### Product Management
- Handles different product categories (food/non-food)
- Applies different markup percentages
- Manages expiration dates
- Applies discounts for near-expiration products
- Prevents sales of expired products

### Exception Handling
- InsufficientQuantityException for stock management
- IllegalStateException for invalid operations
- File handling exceptions for receipt operations

## Example Usage

The `Main` class contains an example of how to use the system:

1. Create a store with specific markup and expiration settings
2. Add products with their details
3. Add cashiers and assign them to registers
4. Process sales and generate receipts
5. View financial reports (expenses, income, profit)

Receipts are automatically saved to files in the project directory with names like `receipt_1.txt` and `receipt_1.ser`.

## Development

This project uses Gradle as the build system. The Gradle Wrapper is included, so you don't need to install Gradle separately. All commands should be run using `./gradlew` (Linux/Mac) or `gradlew.bat` (Windows). 