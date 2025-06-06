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

```
src/
├── main/
│   ├── java/
│   │   └── org/
│   │       ├── Main.java - Application entry point
│   │       ├── model/
│   │       │   ├── Cashier.java - Cashier class with register assignment
│   │       │   ├── Product.java - Product class with expiration date management
│   │       │   ├── ProductCategory.java - Product category enum (FOOD/NON_FOOD)
│   │       │   └── Receipt.java - Receipt generation and serialization
│   │       ├── service/
│   │       │   ├── StoreService.java - Store service interface
│   │       │   └── impl/
│   │       │       └── StoreServiceImpl.java - Store service implementation
│   │       └── exception/
│   │           └── InsufficientQuantityException.java - Custom exception for stock management
│   └── resources/ - Resource files
└── test/
    └── java/
        └── org/
            ├── service/
            │   ├── StoreServiceTest.java - Interface contract tests
            │   └── impl/
            │       └── StoreServiceImplTest.java - Implementation-specific tests
            └── integration/
                └── StoreSystemIntegrationTest.java - End-to-end system tests

build.gradle - Gradle build configuration
gradle/ - Gradle wrapper files
gradlew / gradlew.bat - Gradle wrapper scripts
output/receipts/ - Generated receipt files (gitignored)
README.md - This file
```

## Features in Detail

### Receipt Management
- Receipts are saved in two formats in the `output/receipts/` directory:
  - Text files (`receipt_N.txt`) for human-readable format
  - Serialized files (`receipt_N.ser`) for object persistence
- Receipts can be deserialized (loaded) and read from files
- The output directory is automatically created if it doesn't exist
- Each receipt contains:
  - Unique receipt number
  - Date and time
  - Cashier information
  - List of purchased items
  - Total amount
- Receipts can be loaded from file by their number and printed or processed again (see example below)

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
- Custom exceptions in dedicated `org.exception` package
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
6. **Load and print a receipt from file** (demonstrates deserialization):

   After a sale, the following code is executed in `Main`:

   ```java
   if (store instanceof org.service.impl.StoreServiceImpl) {
       try {
           Receipt loaded = ((org.service.impl.StoreServiceImpl) store).loadReceiptFromFile(receipt.getReceiptNumber());
           System.out.println("\n=== LOADED RECEIPT FROM FILE ===");
           System.out.println(loaded);
       } catch (Exception e) {
           System.out.println("Failed to load receipt from file: " + e.getMessage());
       }
   }
   ```

   This demonstrates how you can restore and display any receipt from disk by its number.

## Testing Strategy

The project follows enterprise-level testing practices with three types of tests:

### Contract Tests (`StoreServiceTest`)
- Test the service interface contract and business logic
- Verify behavior regardless of implementation
- Focus on business rules and expected outcomes
- Use Given-When-Then format for clarity

### Implementation Tests (`StoreServiceImplTest`)
- Test implementation-specific features
- File operations and directory creation
- Error handling and edge cases
- Multiple instance behavior

### Integration Tests (`StoreSystemIntegrationTest`)
- End-to-end testing of complete workflows
- Realistic business scenarios
- Multi-step operations verification
- File generation and persistence testing

## Development

This project uses Gradle as the build system. The Gradle Wrapper is included, so you don't need to install Gradle separately. All commands should be run using `./gradlew` (Linux/Mac) or `gradlew.bat` (Windows). 