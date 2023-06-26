package com.mkh;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        User user1 = User.createUser("Alice", 19);
        User user2 = User.createUser("Bob", 19);
        User user3 = User.createUser("Charlie", 20);
        User user4 = User.createUser("John", 20);

        Product realProduct1 = ProductFactory.createRealProduct("Product A", 20.50, 10, 25);
        Product realProduct2 = ProductFactory.createRealProduct("Product B", 50, 6, 17);

        Product virtualProduct1 = ProductFactory.createVirtualProduct("Product C", 100, "xxx", LocalDate.of(2023, 5, 12));
        Product virtualProduct2 = ProductFactory.createVirtualProduct("Product D", 81.25, "yyy", LocalDate.of(2024, 6, 20));

        List<Order> orders = new ArrayList<>() {{
            add(Order.createOrder(user1, List.of(realProduct1, virtualProduct1, virtualProduct2)));
            add(Order.createOrder(user2, List.of(realProduct1, realProduct2)));
            add(Order.createOrder(user3, List.of(realProduct1, virtualProduct2)));
            add(Order.createOrder(user4, List.of(virtualProduct1, virtualProduct2, realProduct1, realProduct2)));
        }};


        System.out.println("1. Create singleton class VirtualProductCodeManager \n");
        VirtualProductCodeManager virtualProductCodeManager = VirtualProductCodeManager.getInstance();
        var isCodeUsedxxx = virtualProductCodeManager.isCodeUsed("xxx");
        var isCodeUsedyyy = virtualProductCodeManager.isCodeUsed("yyy");
        var isCodeUsedzzz = virtualProductCodeManager.isCodeUsed("zzz");
        System.out.println("Is code used: " + isCodeUsedxxx + "\n");
        System.out.println("Is code used: " + isCodeUsedyyy + "\n");
        System.out.println("Is code used: " + isCodeUsedzzz + "\n");

        Product mostExpensive = getMostExpensiveProduct(orders);
        System.out.println("2. Most expensive product: " + mostExpensive + "\n");

        Product mostPopular = getMostPopularProduct(orders);
        System.out.println("3. Most popular product: " + mostPopular + "\n");

        double averageAge = calculateAverageAge(realProduct2, orders);
        System.out.println("4. Average age is: " + averageAge + "\n");

        Map<Product, List<User>> productUserMap = getProductUserMap(orders);
        System.out.println("5. Map with products as keys and list of users as value \n");
        productUserMap.forEach((key, value) -> System.out.println("key: " + key + " " + "value: " + value + "\n"));

        List<Product> productsByPrice = sortProductsByPrice(List.of(realProduct1, realProduct2, virtualProduct1, virtualProduct2));
        System.out.println("6. a) List of products sorted by price: " + productsByPrice + "\n");
        List<Order> ordersByUserAgeDesc = sortOrdersByUserAgeDesc(orders);
        System.out.println("6. b) List of orders sorted by user agge in descending order: " + ordersByUserAgeDesc + "\n");

        Map<Order, Integer> result = calculateWeightOfEachOrder(orders);
        System.out.println("7. Calculate the total weight of each order \n");
        result.forEach((key, value) -> System.out.println("order: " + key + " " + "total weight: " + value + "\n"));
    }

    private static Product getMostExpensiveProduct(List<Order> orders) {
        return checkContainingAlLeastOneProduct(orders) ? orders.stream()
                .map(Order::getProducts)
                .flatMap(Collection::stream)
                .max(Comparator.comparing(Product::getPrice))
                .get() : null;
    }

    private static Product getMostPopularProduct(List<Order> orders) {
        return checkContainingAlLeastOneProduct(orders) ? orders.stream()
                .map(Order::getProducts)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(product -> product, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .get() : null;
    }

    private static double calculateAverageAge(Product product, List<Order> orders) {
        return orders.parallelStream()
                .filter(order -> order.getProducts().contains(product))
                .map(Order::getUser)
                .map(User::getAge)
                .mapToInt(Integer::intValue).average().orElse(0);
    }

    private static Map<Product, List<User>> getProductUserMap(List<Order> orders) {
        return orders.stream()
                .map(Order::getProducts)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(
                        product -> product,
                        x -> orders.stream()
                                .filter(order -> order.getProducts().contains(x))
                                .map(Order::getUser)
                                .collect(Collectors.toList()))
                );
    }

    private static List<Product> sortProductsByPrice(List<Product> products) {
        return products.stream()
                .sorted(Comparator.comparing(Product::getPrice))
                .collect(Collectors.toList());
    }

    private static List<Order> sortOrdersByUserAgeDesc(List<Order> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(order -> -1 * order.getUser().getAge()))
                .collect(Collectors.toList());
    }

    private static Map<Order, Integer> calculateWeightOfEachOrder(List<Order> orders) {
        return orders.parallelStream()
                .collect(Collectors.toMap(
                        order -> order,
                        order -> order.getProducts().parallelStream()
                                .filter(RealProduct.class::isInstance)
                                .mapToInt(product -> ((RealProduct) product).getWeight())
                                .sum()
                ));
    }

    private static boolean checkContainingAlLeastOneProduct(List<Order> orders) {
        for (Order order : orders) {
            if (order.getProducts() != null && order.getProducts().size() > 0)
                return true;
        }
        return false;
    }

}

class User {
    private String name;
    private int age;

    private User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public static User createUser(String name, int age) {
        return new User(name, age);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

abstract class Product {
    String name;
    double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

class RealProduct extends Product {
    private int size;
    private int weight;

    public RealProduct(String name, double price, int size, int weight) {
        super(name, price);
        this.size = size;
        this.weight = weight;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "RealProduct{" +
                "size=" + size +
                ", weight=" + weight +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}

class VirtualProduct extends Product {
    private String code;
    private LocalDate experiencedDate;

    public VirtualProduct(String name, double price, String code, LocalDate experiencedDate) {
        super(name, price);
        this.code = code;
        this.experiencedDate = experiencedDate;
        useCode();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        useCode();
    }

    public LocalDate getExperiencedDate() {
        return experiencedDate;
    }

    public void setExperiencedDate(LocalDate experiencedDate) {
        this.experiencedDate = experiencedDate;
    }

    private void useCode() {
        VirtualProductCodeManager.getInstance().useCode(this.code);
    }

    @Override
    public String toString() {
        return "VirtualProduct{" +
                "code='" + code + '\'' +
                ", experiencedDate=" + experiencedDate +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}

class ProductFactory {
    public static RealProduct createRealProduct(String name, double price, int size, int weight) {
        return new RealProduct(name, price, size, weight);
    }

    public static VirtualProduct createVirtualProduct(String name, double price, String code, LocalDate experiencedDate) {
        return new VirtualProduct(name, price, code, experiencedDate);
    }
}

class Order {
    private User user;
    private List<Product> products;

    private Order(User user, List<Product> products) {
        this.user = user;
        this.products = products;
    }

    public static Order createOrder(User user, List<Product> realProduct) {
        return new Order(user, realProduct);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "Order{" +
                "user=" + user +
                ", products=" + products +
                '}';
    }
}

class VirtualProductCodeManager {
    private volatile static VirtualProductCodeManager virtualProductCodeManager;

    private List<String> usedCodes;

    private VirtualProductCodeManager() {
        usedCodes = new ArrayList<>();
    }

    public static VirtualProductCodeManager getInstance() {
        if (virtualProductCodeManager == null) {
            synchronized (VirtualProductCodeManager.class) {
                if (virtualProductCodeManager == null) {
                    virtualProductCodeManager = new VirtualProductCodeManager();
                }
            }
        }
        return virtualProductCodeManager;
    }

    public void useCode(String code) {
        usedCodes.add(code);
    }

    public boolean isCodeUsed(String code) {
        return usedCodes.contains(code);
    }

}