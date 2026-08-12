# KiruthikaMart
E commerce console project 

import java.util.*;

public class BookMart {

    static Scanner sc = new Scanner(System.in);

    static class Book {
        int id, stock;
        String title, author, category;
        double price;

        Book(int id, String title, String author,
             String category, double price, int stock) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
            this.price = price;
            this.stock = stock;
        }
    }

    static ArrayList<Book> books = new ArrayList<>();
    static ArrayList<Book> cart = new ArrayList<>();
    static ArrayList<Integer> quantities = new ArrayList<>();

    static void addBooks() {
        books.add(new Book(1, "Java Programming",
                "Herbert Schildt", "Programming", 450, 10));

        books.add(new Book(2, "Python Basics",
                "Mark Lutz", "Programming", 400, 8));

        books.add(new Book(3, "Data Science",
                "John Smith", "Data Science", 550, 5));

        books.add(new Book(4, "Artificial Intelligence",
                "Stuart Russell", "AI", 650, 6));
    }

    static void displayBooks() {
        System.out.println("\n========== AVAILABLE BOOKS ==========");

        for (Book b : books) {
            System.out.println(
                "ID: " + b.id +
                " | " + b.title +
                " | Author: " + b.author +
                " | Category: " + b.category +
                " | Price: Rs." + b.price +
                " | Stock: " + b.stock
            );
        }
    }

    static void searchBook() {
        sc.nextLine();

        System.out.print("\nEnter book title/author/category: ");
        String search = sc.nextLine().toLowerCase();

        boolean found = false;

        for (Book b : books) {
            if (b.title.toLowerCase().contains(search)
                    || b.author.toLowerCase().contains(search)
                    || b.category.toLowerCase().contains(search)) {

                System.out.println(
                    b.id + " - " + b.title +
                    " | " + b.author +
                    " | Rs." + b.price +
                    " | Stock: " + b.stock
                );

                found = true;
            }
        }

        if (!found)
            System.out.println("Book not found!");
    }

    static void addToCart() {
        displayBooks();

        System.out.print("\nEnter Book ID: ");
        int id = sc.nextInt();

        Book selected = null;

        for (Book b : books) {
            if (b.id == id) {
                selected = b;
                break;
            }
        }

        if (selected == null) {
            System.out.println("Invalid Book ID!");
            return;
        }

        System.out.print("Enter quantity: ");
        int qty = sc.nextInt();

        if (qty <= 0 || qty > selected.stock) {
            System.out.println("Insufficient stock!");
            return;
        }

        cart.add(selected);
        quantities.add(qty);

        System.out.println("Book added to cart successfully!");
    }

    static void viewCart() {

        if (cart.isEmpty()) {
            System.out.println("\nCart is empty!");
            return;
        }

        double total = 0;

        System.out.println("\n========== YOUR CART ==========");

        for (int i = 0; i < cart.size(); i++) {

            Book b = cart.get(i);
            int qty = quantities.get(i);

            double subtotal = b.price * qty;
            total += subtotal;

            System.out.println(
                b.title +
                " | Quantity: " + qty +
                " | Subtotal: Rs." + subtotal
            );
        }

        System.out.println("--------------------------------");
        System.out.println("Total Amount: Rs." + total);
    }

    static void checkout() {

        if (cart.isEmpty()) {
            System.out.println("\nCart is empty!");
            return;
        }

        double total = 0;

        for (int i = 0; i < cart.size(); i++) {
            Book b = cart.get(i);
            int qty = quantities.get(i);

            total += b.price * qty;
        }

        System.out.println("\n========== CHECKOUT ==========");
        System.out.println("Total Amount: Rs." + total);

        System.out.print("Confirm payment? (yes/no): ");
        String answer = sc.next();

        if (answer.equalsIgnoreCase("yes")) {

            for (int i = 0; i < cart.size(); i++) {
                Book b = cart.get(i);
                int qty = quantities.get(i);

                b.stock -= qty;
            }

            System.out.println("\nPayment Successful!");
            System.out.println("Order Confirmed!");
            System.out.println("Order Status: PENDING");
            System.out.println("Order Status Flow:");
            System.out.println("PENDING -> CONFIRMED -> SHIPPED -> DELIVERED");

            cart.clear();
            quantities.clear();

        } else {
            System.out.println("Order cancelled.");
        }
    }

    static void reviewBook() {

        sc.nextLine();

        System.out.print("\nEnter book name: ");
        String name = sc.nextLine();

        System.out.print("Give rating (1-5): ");
        int rating = sc.nextInt();

        sc.nextLine();

        System.out.print("Enter your comment: ");
        String comment = sc.nextLine();

        if (rating >= 1 && rating <= 5) {
            System.out.println("\nReview submitted successfully!");
            System.out.println("Book: " + name);
            System.out.println("Rating: " + rating + "/5");
            System.out.println("Comment: " + comment);
        } else {
            System.out.println("Invalid rating!");
        }
    }

    public static void main(String[] args) {

        addBooks();

        System.out.println("======================================");
        System.out.println("        WELCOME TO BOOKMART");
        System.out.println("      ONLINE BOOK MANAGEMENT");
        System.out.println("======================================");

        System.out.print("\nEnter Username: ");
        String username = sc.next();

        System.out.print("Enter Password: ");
        String password = sc.next();

        System.out.println("\nLogin Successful!");
        System.out.println("Welcome " + username);

        int choice;

        do {
            System.out.println("\n========== BOOKMART MENU ==========");
            System.out.println("1. View Books");
            System.out.println("2. Search Books");
            System.out.println("3. Add Book to Cart");
            System.out.println("4. View Cart");
            System.out.println("5. Checkout");
            System.out.println("6. Give Review");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    displayBooks();
                    break;

                case 2:
                    searchBook();
                    break;

                case 3:
                    addToCart();
                    break;

                case 4:
                    viewCart();
                    break;

                case 5:
                    checkout();
                    break;

                case 6:
                    reviewBook();
                    break;

                case 7:
                    System.out.println(
                        "\nThank you for using BookMart!"
                    );
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 7);

        sc.close();
    }
}
