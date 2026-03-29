package com.ecommerce.product.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.product.models.Product;
import com.ecommerce.product.repositories.ProductRepository;

@Configuration
public class ProductDataInitializer {

    @Bean
    CommandLineRunner initProductData(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() < 30) {

                List<Product> products = Arrays.asList(

                    // ── ELECTRONICS ──────────────────────────────────────────────────────────

                    createProduct(
                        "Sony WH-1000XM5 Wireless Headphones",
                        "Industry-leading noise cancellation with up to 30-hour battery life and multipoint Bluetooth pairing.",
                        new BigDecimal("349.99"), new BigDecimal("299.99"),
                        45, "Electronics",
                        Arrays.asList(
                            "https://images.pexels.com/photos/3394655/pexels-photo-3394655.jpeg",
                            "https://images.pexels.com/photos/3587478/pexels-photo-3587478.jpeg"
                        ),
                        Arrays.asList("One Size"), Arrays.asList("Midnight Black", "Platinum Silver"),
                        "Tech", "Sony", 4.8, 1284
                    ),

                    createProduct(
                        "Apple MacBook Air M3",
                        "Ultra-thin laptop powered by the Apple M3 chip with 18-hour battery life and a stunning Retina display.",
                        new BigDecimal("1299.99"), null,
                        20, "Electronics",
                        Arrays.asList(
                            "https://images.pexels.com/photos/812264/pexels-photo-812264.jpeg",
                            "https://images.pexels.com/photos/1029757/pexels-photo-1029757.jpeg"
                        ),
                        Arrays.asList("13-inch", "15-inch"),
                        Arrays.asList("Midnight", "Starlight", "Space Gray"),
                        "Tech", "Apple", 4.9, 3412
                    ),

                    createProduct(
                        "Logitech MX Master 3S Mouse",
                        "Advanced wireless mouse with ultra-fast MagSpeed scrolling, 8K DPI, and whisper-quiet clicks.",
                        new BigDecimal("99.99"), new BigDecimal("84.99"),
                        60, "Electronics",
                        Arrays.asList(
                            "https://images.pexels.com/photos/845434/pexels-photo-845434.jpeg",
                            "https://images.pexels.com/photos/2115257/pexels-photo-2115257.jpeg"
                        ),
                        Arrays.asList("Standard"),
                        Arrays.asList("Graphite", "Pale Gray"),
                        "Tech", "Logitech", 4.7, 876
                    ),

                    createProduct(
                        "Samsung 27\" QHD Monitor",
                        "27-inch QHD IPS monitor with 165Hz refresh rate, 1ms response time, and AMD FreeSync Premium.",
                        new BigDecimal("449.99"), new BigDecimal("379.99"),
                        18, "Electronics",
                        Arrays.asList(
                            "https://images.pexels.com/photos/1714208/pexels-photo-1714208.jpeg"
                        ),
                        Arrays.asList("27-inch"),
                        Arrays.asList("Black"),
                        "Tech", "Samsung", 4.6, 542
                    ),

                    createProduct(
                        "Apple Watch Series 9",
                        "Advanced health sensors including blood oxygen, ECG, and crash detection with Always-On Retina display.",
                        new BigDecimal("399.99"), new BigDecimal("349.99"),
                        35, "Wearables",
                        Arrays.asList(
                            "https://images.pexels.com/photos/267394/pexels-photo-267394.jpeg",
                            "https://images.pexels.com/photos/393047/pexels-photo-393047.jpeg"
                        ),
                        Arrays.asList("41mm", "45mm"),
                        Arrays.asList("Midnight", "Starlight", "Product RED"),
                        "Sport", "Apple", 4.8, 2107
                    ),

                    createProduct(
                        "Anker 65W USB-C Charger",
                        "GaN II fast charger with 3 ports supporting simultaneous charging for laptop, phone, and tablet.",
                        new BigDecimal("45.99"), null,
                        120, "Electronics",
                        Arrays.asList(
                            "https://images.pexels.com/photos/4219654/pexels-photo-4219654.jpeg"
                        ),
                        Arrays.asList("One Size"),
                        Arrays.asList("Black", "White"),
                        "Tech", "Anker", 4.5, 678
                    ),

                    // ── FASHION – T-SHIRTS ────────────────────────────────────────────────────

                    createProduct(
                        "Uniqlo Supima Cotton T-Shirt",
                        "Premium Supima cotton crew-neck tee with a relaxed fit and superior softness.",
                        new BigDecimal("19.99"), null,
                        150, "T-Shirt",
                        Arrays.asList(
                            "https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg",
                            "https://images.pexels.com/photos/1656684/pexels-photo-1656684.jpeg"
                        ),
                        Arrays.asList("XS", "S", "M", "L", "XL", "XXL"),
                        Arrays.asList("White", "Black", "Navy", "Olive"),
                        "Casual", "Uniqlo", 4.5, 892
                    ),

                    createProduct(
                        "Adidas Techfit Training T-Shirt",
                        "Moisture-wicking AEROREADY fabric keeps you dry during intense training sessions.",
                        new BigDecimal("34.99"), new BigDecimal("27.99"),
                        80, "T-Shirt",
                        Arrays.asList(
                            "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg"
                        ),
                        Arrays.asList("S", "M", "L", "XL"),
                        Arrays.asList("Black", "Gray", "Royal Blue"),
                        "Gym", "Adidas", 4.4, 415
                    ),

                    createProduct(
                        "Ralph Lauren Polo Shirt",
                        "Iconic piqué polo with embroidered Polo Pony logo, perfect for smart-casual occasions.",
                        new BigDecimal("89.99"), null,
                        65, "T-Shirt",
                        Arrays.asList(
                            "https://images.pexels.com/photos/428340/pexels-photo-428340.jpeg"
                        ),
                        Arrays.asList("S", "M", "L", "XL"),
                        Arrays.asList("Navy", "White", "Forest Green"),
                        "Formal", "Ralph Lauren", 4.7, 631
                    ),

                    // ── FASHION – SHIRTS ──────────────────────────────────────────────────────

                    createProduct(
                        "Calvin Klein Slim-Fit Dress Shirt",
                        "Non-iron cotton dress shirt with a tailored slim fit, ideal for business and formal events.",
                        new BigDecimal("79.99"), new BigDecimal("64.99"),
                        55, "Shirts",
                        Arrays.asList(
                            "https://images.pexels.com/photos/887352/pexels-photo-887352.jpeg"
                        ),
                        Arrays.asList("S", "M", "L", "XL"),
                        Arrays.asList("White", "Light Blue", "Charcoal"),
                        "Formal", "Calvin Klein", 4.6, 478
                    ),

                    createProduct(
                        "Uniqlo Premium Linen Shirt",
                        "Lightweight 100% linen shirt that stays cool in tropical climates — perfect for everyday wear.",
                        new BigDecimal("49.99"), new BigDecimal("39.99"),
                        70, "Shirts",
                        Arrays.asList(
                            "https://images.pexels.com/photos/1336873/pexels-photo-1336873.jpeg"
                        ),
                        Arrays.asList("S", "M", "L", "XL"),
                        Arrays.asList("Beige", "Light Blue", "Sage Green"),
                        "Casual", "Uniqlo", 4.3, 324
                    ),

                    createProduct(
                        "Under Armour Stretch Woven Shirt",
                        "4-way stretch training shirt with anti-odour technology for peak athletic performance.",
                        new BigDecimal("54.99"), null,
                        60, "Shirts",
                        Arrays.asList(
                            "https://images.pexels.com/photos/2269872/pexels-photo-2269872.jpeg"
                        ),
                        Arrays.asList("S", "M", "L", "XL"),
                        Arrays.asList("Black", "Steel Gray"),
                        "Gym", "Under Armour", 4.4, 289
                    ),

                    // ── FASHION – HOODIES ─────────────────────────────────────────────────────

                    createProduct(
                        "Champion Reverse Weave Hoodie",
                        "Classic heavyweight fleece hoodie built to resist shrinkage — a streetwear staple.",
                        new BigDecimal("65.99"), new BigDecimal("54.99"),
                        50, "Hoodie",
                        Arrays.asList(
                            "https://images.pexels.com/photos/6311399/pexels-photo-6311399.jpeg"
                        ),
                        Arrays.asList("S", "M", "L", "XL", "XXL"),
                        Arrays.asList("Black", "Oxford Gray", "Navy"),
                        "Casual", "Champion", 4.6, 743
                    ),

                    createProduct(
                        "Adidas Essentials Full-Zip Hoodie",
                        "Soft French terry zip hoodie with kangaroo pockets and ribbed cuffs for gym-to-street versatility.",
                        new BigDecimal("59.99"), null,
                        45, "Hoodie",
                        Arrays.asList(
                            "https://images.pexels.com/photos/7675413/pexels-photo-7675413.jpeg"
                        ),
                        Arrays.asList("S", "M", "L", "XL"),
                        Arrays.asList("Black", "Dark Blue"),
                        "Gym", "Adidas", 4.4, 512
                    ),

                    createProduct(
                        "Hugo Boss Minimal Loopback Hoodie",
                        "Premium cotton-blend hoodie with tonal logo embroidery, tailored for smart-casual styling.",
                        new BigDecimal("149.99"), null,
                        25, "Hoodie",
                        Arrays.asList(
                            "https://images.pexels.com/photos/921265/pexels-photo-921265.jpeg"
                        ),
                        Arrays.asList("M", "L", "XL"),
                        Arrays.asList("Charcoal", "Navy"),
                        "Formal", "Hugo Boss", 4.8, 298
                    ),

                    // ── FASHION – JEANS ───────────────────────────────────────────────────────

                    createProduct(
                        "Levi's 511 Slim Fit Jeans",
                        "Iconic slim-cut stretch denim that sits below the waist with a close fit through hip and thigh.",
                        new BigDecimal("69.99"), new BigDecimal("54.99"),
                        75, "Jeans",
                        Arrays.asList(
                            "https://images.pexels.com/photos/2983462/pexels-photo-2983462.jpeg"
                        ),
                        Arrays.asList("28", "30", "32", "34", "36"),
                        Arrays.asList("Indigo", "Black", "Dark Wash"),
                        "Casual", "Levi's", 4.6, 1456
                    ),

                    createProduct(
                        "Wrangler Authentics Regular Fit Jeans",
                        "Classic 5-pocket dark-wash denim with a relaxed regular fit — versatile for work and weekend.",
                        new BigDecimal("59.99"), null,
                        60, "Jeans",
                        Arrays.asList(
                            "https://images.pexels.com/photos/298863/pexels-photo-298863.jpeg"
                        ),
                        Arrays.asList("30", "32", "34", "36", "38"),
                        Arrays.asList("Dark Indigo"),
                        "Formal", "Wrangler", 4.4, 632
                    ),

                    createProduct(
                        "Lee Extreme Motion Stretch Jeans",
                        "Flex fabric that moves with you during workouts and everyday activity without losing its shape.",
                        new BigDecimal("64.99"), new BigDecimal("52.99"),
                        40, "Jeans",
                        Arrays.asList(
                            "https://images.pexels.com/photos/532556/pexels-photo-532556.jpeg"
                        ),
                        Arrays.asList("30", "32", "34", "36"),
                        Arrays.asList("Black", "Midnight Gray"),
                        "Gym", "Lee", 4.3, 378
                    ),

                    // ── FASHION – SHORTS ─────────────────────────────────────────────────────

                    createProduct(
                        "Nike Dri-FIT Running Shorts",
                        "Lightweight shorts with sweat-wicking Dri-FIT fabric and an inner brief for full-range motion.",
                        new BigDecimal("34.99"), new BigDecimal("27.99"),
                        90, "Shorts",
                        Arrays.asList(
                            "https://images.pexels.com/photos/3775537/pexels-photo-3775537.jpeg"
                        ),
                        Arrays.asList("S", "M", "L", "XL"),
                        Arrays.asList("Black", "Navy", "Dark Gray"),
                        "Gym", "Nike", 4.5, 714
                    ),

                    createProduct(
                        "Gap Lived-In Khaki Shorts",
                        "Relaxed fit chino shorts in a soft-washed cotton twill — an everyday-wear essential.",
                        new BigDecimal("39.99"), null,
                        85, "Shorts",
                        Arrays.asList(
                            "https://images.pexels.com/photos/2529147/pexels-photo-2529147.jpeg"
                        ),
                        Arrays.asList("28", "30", "32", "34", "36"),
                        Arrays.asList("Khaki", "Beige", "Olive"),
                        "Casual", "Gap", 4.2, 385
                    ),

                    createProduct(
                        "Dockers Smart Flex Chino Shorts",
                        "Tailored chino shorts with stretch comfort waistband — office-ready yet casual enough for weekends.",
                        new BigDecimal("49.99"), null,
                        45, "Shorts",
                        Arrays.asList(
                            "https://images.pexels.com/photos/631139/pexels-photo-631139.jpeg"
                        ),
                        Arrays.asList("30", "32", "34", "36"),
                        Arrays.asList("Beige", "Light Gray"),
                        "Formal", "Dockers", 4.4, 267
                    ),

                    // ── FOOTWEAR ──────────────────────────────────────────────────────────────

                    createProduct(
                        "Nike Air Zoom Pegasus 41",
                        "Responsive running shoe with Air Zoom cushioning and engineered mesh upper for breathability.",
                        new BigDecimal("129.99"), new BigDecimal("109.99"),
                        30, "Footwear",
                        Arrays.asList(
                            "https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg",
                            "https://images.pexels.com/photos/1598505/pexels-photo-1598505.jpeg"
                        ),
                        Arrays.asList("7", "8", "9", "10", "11", "12"),
                        Arrays.asList("Black/White", "Blue/White", "Wolf Gray"),
                        "Athletic", "Nike", 4.8, 2341
                    ),

                    createProduct(
                        "Adidas Ultraboost 22",
                        "Energy-returning Boost midsole with a Primeknit+ upper delivering responsive cushioning on long runs.",
                        new BigDecimal("179.99"), new BigDecimal("149.99"),
                        22, "Footwear",
                        Arrays.asList(
                            "https://images.pexels.com/photos/1454171/pexels-photo-1454171.jpeg"
                        ),
                        Arrays.asList("7", "8", "9", "10", "11"),
                        Arrays.asList("Core Black", "Cloud White"),
                        "Athletic", "Adidas", 4.7, 1789
                    ),

                    // ── ACCESSORIES ───────────────────────────────────────────────────────────

                    createProduct(
                        "SwissGear Scansmart Laptop Backpack",
                        "TSA-friendly backpack with ScanSmart technology, dedicated 17-inch laptop compartment and USB port.",
                        new BigDecimal("89.99"), new BigDecimal("74.99"),
                        40, "Accessories",
                        Arrays.asList(
                            "https://images.pexels.com/photos/3747463/pexels-photo-3747463.jpeg",
                            "https://images.pexels.com/photos/2905238/pexels-photo-2905238.jpeg"
                        ),
                        Arrays.asList("One Size"),
                        Arrays.asList("Black", "Navy"),
                        "Casual", "SwissGear", 4.7, 1092
                    ),

                    createProduct(
                        "Fossil Gen 6 Hybrid Smartwatch",
                        "AMOLED display hybrid smartwatch with SpO2 sensor, sleep tracking, and 2-week battery life.",
                        new BigDecimal("199.99"), new BigDecimal("169.99"),
                        28, "Wearables",
                        Arrays.asList(
                            "https://images.pexels.com/photos/125779/pexels-photo-125779.jpeg"
                        ),
                        Arrays.asList("42mm", "46mm"),
                        Arrays.asList("Smoke Steel", "Brown Leather"),
                        "Sport", "Fossil", 4.4, 567
                    ),

                    createProduct(
                        "Starbucks Ceramic Travel Mug",
                        "Double-wall ceramic mug with a secure lid, keeps beverages hot for 4 hours — hand-wash only.",
                        new BigDecimal("24.99"), null,
                        200, "Home & Kitchen",
                        Arrays.asList(
                            "https://images.pexels.com/photos/585750/pexels-photo-585750.jpeg"
                        ),
                        Arrays.asList("350ml", "480ml"),
                        Arrays.asList("White", "Black", "Rose Quartz"),
                        "Classic", "Starbucks", 4.2, 389
                    ),

                    createProduct(
                        "Bellroy Tech Kit Organiser",
                        "Compact zippered organiser for cables, adapters, and small tech accessories — fits in most bags.",
                        new BigDecimal("49.99"), null,
                        55, "Accessories",
                        Arrays.asList(
                            "https://images.pexels.com/photos/4219654/pexels-photo-4219654.jpeg"
                        ),
                        Arrays.asList("One Size"),
                        Arrays.asList("Black", "Basalt"),
                        "Tech", "Bellroy", 4.6, 412
                    )

                );

                productRepository.saveAll(products);
                System.out.println("✅ Product Service: 30 demo products loaded into PostgreSQL.");
            }
        };
    }

    private Product createProduct(
            String name, String description,
            BigDecimal price, BigDecimal discountPrice,
            int stock, String category,
            List<String> imageUrls,
            List<String> sizes, List<String> colors,
            String dressStyle, String brand,
            double rating, int numReviews) {

        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setDiscountPrice(discountPrice);
        p.setStockQuantity(stock);
        p.setCategory(category);
        p.setImageUrls(imageUrls);
        p.setSizes(sizes);
        p.setColors(colors);
        p.setDressStyle(dressStyle);
        p.setBrand(brand);
        p.setRating(rating);
        p.setNumReviews(numReviews);
        p.setActive(true);
        p.setSellerId("demo-seller-001");
        p.setSellerName("Demo Store");
        return p;
    }

}