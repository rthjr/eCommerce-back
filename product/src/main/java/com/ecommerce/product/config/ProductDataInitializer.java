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
                            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1615750173692-ca4e8c5a21ca?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1593640408182-31c228b59f51?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1551816230-ef5deaed4a26?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1591370874773-6702e8f12fd8?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1585338107529-13afc5f02586?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1622445275576-721325763afe?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1571945153237-4929e783af4a?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1594938298603-c8148c4b4733?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1603252109303-2751441dd157?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1602810316693-3667c854239a?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1574680096145-d05b474e2155?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1618354691373-d851c5c3a990?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1556821840-3a63f15732ce?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1578768079052-aa76e52ff62e?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1611911813383-67769b37a149?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1565693413579-8ff3fdc1b03b?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1475178626620-a4d074967452?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1555689502-c4b22d76c56f?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1604176354204-9268737828e4?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1582552938357-32b906df40cb?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1591195853828-11db59a44f43?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1571731956672-f2b94d7dd0cb?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1565084888279-aca607ecce0c?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1509551388413-e18d0ac5d495?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1608231387042-66d1773070a5?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1587563871167-1ee9c731aefb?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1622560480605-d83c853bc5c3?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=600&q=80"
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
                            "https://images.unsplash.com/photo-1625948515291-69613efd103f?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1586953208270-31a4db8f9fe1?auto=format&fit=crop&w=600&q=80"
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