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
			if (productRepository.count() < 28) {

				List<Product> products = Arrays.asList(

						// ------------------------------------------------------------
						// ENHANCED PRODUCTS WITH ALL NEW FIELDS
						// ------------------------------------------------------------
						createProductWithDetails("Wireless Headphones",
								"High-quality Bluetooth headphones with noise cancellation",
								new BigDecimal("79.99"), new BigDecimal("64.99"), 50, "Electronics",
								Arrays.asList("https://images.pexels.com/photos/3394655/pexels-photo-3394655.jpeg",
										"https://images.pexels.com/photos/3587478/pexels-photo-3587478.jpeg"),
								Arrays.asList("One Size"), Arrays.asList("Black", "White"), "Tech", "Sony", 4.5, 342),

						createProductWithDetails("Laptop Backpack", "Durable waterproof backpack for 15-inch laptops",
								new BigDecimal("49.99"), null, 40, "Accessories",
								Arrays.asList("https://images.pexels.com/photos/3747463/pexels-photo-3747463.jpeg",
										"https://images.pexels.com/photos/2905238/pexels-photo-2905238.jpeg"),
								Arrays.asList("Medium", "Large"), Arrays.asList("Black", "Gray"), "Casual", "SwissGear",
								4.7, 256),

						createProductWithDetails("Smart Watch", "Track your health and fitness in style",
								new BigDecimal("129.99"), new BigDecimal("99.99"), 30, "Wearables",
								Arrays.asList("https://images.pexels.com/photos/267394/pexels-photo-267394.jpeg",
										"https://images.pexels.com/photos/393047/pexels-photo-393047.jpeg"),
								Arrays.asList("38mm", "42mm"), Arrays.asList("Silver", "Gold", "Black"), "Sport",
								"Apple", 4.8, 892),

						createProductWithDetails("Coffee Mug", "Ceramic mug with heat-resistant coating",
								new BigDecimal("9.99"), null, 100, "Home & Kitchen",
								Arrays.asList("https://images.pexels.com/photos/585750/pexels-photo-585750.jpeg"),
								Arrays.asList("350ml", "500ml"), Arrays.asList("White", "Blue", "Red"), "Classic",
								"Starbucks", 4.2, 145),

						createProductWithDetails("Gaming Mouse", "Ergonomic RGB gaming mouse with high DPI sensor",
								new BigDecimal("39.99"), new BigDecimal("34.99"), 60, "Electronics",
								Arrays.asList("https://images.pexels.com/photos/845434/pexels-photo-845434.jpeg",
										"https://images.pexels.com/photos/2115257/pexels-photo-2115257.jpeg"),
								Arrays.asList("Standard"), Arrays.asList("Black", "RGB"), "Gaming", "Logitech", 4.6,
								523),

						createProductWithDetails("Cotton T-Shirt", "Comfortable 100% cotton t-shirt",
								new BigDecimal("19.99"), new BigDecimal("14.99"), 80, "T-shirt",
								Arrays.asList("https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg",
										"https://images.pexels.com/photos/1656684/pexels-photo-1656684.jpeg"),
								Arrays.asList("S", "M", "L", "XL"), Arrays.asList("White", "Black", "Navy"), "Casual",
								"H&M", 4.3, 287),

						createProductWithDetails("Running Shoes", "Lightweight running shoes with cushioned sole",
								new BigDecimal("89.99"), new BigDecimal("74.99"), 25, "Footwear",
								Arrays.asList("https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg",
										"https://images.pexels.com/photos/1598505/pexels-photo-1598505.jpeg"),
								Arrays.asList("7", "8", "9", "10", "11"), Arrays.asList("Black", "White", "Blue"),
								"Athletic", "Nike", 4.9, 1024),

						createProductWithDetails("Denim Jeans", "Classic fit denim jeans",
								new BigDecimal("59.99"), null, 35, "Jeans",
								Arrays.asList("https://images.pexels.com/photos/1598507/pexels-photo-1598507.jpeg",
										"https://images.pexels.com/photos/1598508/pexels-photo-1598508.jpeg"),
								Arrays.asList("28", "30", "32", "34", "36"), Arrays.asList("Blue", "Black"), "Casual",
								"Levi's", 4.4, 412),

						// ---------- T-SHIRTS ----------
						createProductWithDetails("Premium Gym T-Shirt", "Moisture-wicking gym T-shirt",
								new BigDecimal("24.99"), new BigDecimal("19.99"), 70, "T-shirt",
								Arrays.asList("https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg"),
								Arrays.asList("S", "M", "L", "XL"), Arrays.asList("Black", "Gray", "Blue"), "Gym",
								"Adidas", 4.1, 198),

						createProductWithDetails("Casual Oversize T-Shirt", "Soft cotton oversize tee",
								new BigDecimal("22.99"), null, 90, "T-shirt",
								Arrays.asList("https://images.pexels.com/photos/1002640/pexels-photo-1002640.jpeg"),
								Arrays.asList("M", "L", "XL"), Arrays.asList("White", "Beige"), "Casual", "Zara", 3.9,
								156),

						createProductWithDetails("Party Graphic T-Shirt", "Trendy graphic tee for party nights",
								new BigDecimal("29.99"), new BigDecimal("24.99"), 65, "T-shirt",
								Arrays.asList("https://images.pexels.com/photos/1036623/pexels-photo-1036623.jpeg"),
								Arrays.asList("S", "M", "L"), Arrays.asList("Black", "Red"), "Party", "Puma", 4.0, 134),

						createProductWithDetails("Formal Polo T-Shirt", "Elegant polo for semi-formal wear",
								new BigDecimal("34.99"), null, 50, "T-shirt",
								Arrays.asList("https://images.pexels.com/photos/428340/pexels-photo-428340.jpeg"),
								Arrays.asList("M", "L", "XL"), Arrays.asList("Navy", "White"), "Formal", "Ralph Lauren",
								4.6, 289),

						// ---------- SHORTS ----------
						createProductWithDetails("Running Gym Shorts", "Breathable athletic shorts",
								new BigDecimal("19.99"), new BigDecimal("16.99"), 80, "Shorts",
								Arrays.asList("https://images.pexels.com/photos/3775537/pexels-photo-3775537.jpeg"),
								Arrays.asList("M", "L", "XL"), Arrays.asList("Black", "Blue", "Gray"), "Gym", "Nike",
								4.4, 223),

						createProductWithDetails("Casual Cotton Shorts", "Lightweight everyday shorts",
								new BigDecimal("17.99"), null, 100, "Shorts",
								Arrays.asList("https://images.pexels.com/photos/2529147/pexels-photo-2529147.jpeg"),
								Arrays.asList("S", "M", "L", "XL"), Arrays.asList("Khaki", "Beige", "Navy"), "Casual",
								"Gap", 3.8, 167),

						createProductWithDetails("Party Denim Shorts", "Stylish short denim jeans",
								new BigDecimal("27.99"), new BigDecimal("22.99"), 60, "Shorts",
								Arrays.asList("https://images.pexels.com/photos/2983464/pexels-photo-2983464.jpeg"),
								Arrays.asList("28", "30", "32", "34"), Arrays.asList("Blue", "Black"), "Party",
								"Levi's", 4.2, 189),

						createProductWithDetails("Formal Chino Shorts", "Dressy chino shorts",
								new BigDecimal("32.99"), null, 40, "Shorts",
								Arrays.asList("https://images.pexels.com/photos/631139/pexels-photo-631139.jpeg"),
								Arrays.asList("30", "32", "34", "36"), Arrays.asList("Beige", "Gray"), "Formal",
								"Dockers", 4.3, 145),

						// ---------- SHIRTS ----------
						createProductWithDetails("Formal White Shirt", "Classic long-sleeve formal shirt",
								new BigDecimal("39.99"), null, 55, "Shirts",
								Arrays.asList("https://images.pexels.com/photos/887352/pexels-photo-887352.jpeg"),
								Arrays.asList("M", "L", "XL"), Arrays.asList("White"), "Formal", "Calvin Klein", 4.7,
								312),

						createProductWithDetails("Casual Linen Shirt", "Breathable linen shirt",
								new BigDecimal("34.99"), new BigDecimal("29.99"), 60, "Shirts",
								Arrays.asList("https://images.pexels.com/photos/1336873/pexels-photo-1336873.jpeg"),
								Arrays.asList("M", "L"), Arrays.asList("Beige", "Light Blue"), "Casual", "Uniqlo", 4.1,
								178),

						createProductWithDetails("Party Printed Shirt", "Eye-catching printed shirt",
								new BigDecimal("44.99"), new BigDecimal("39.99"), 40, "Shirts",
								Arrays.asList("https://images.pexels.com/photos/1043474/pexels-photo-1043474.jpeg"),
								Arrays.asList("M", "L", "XL"), Arrays.asList("Black", "Red"), "Party", "Versace", 4.5,
								234),

						createProductWithDetails("Slim-Fit Gym Shirt", "Stretch-fit shirt for workouts",
								new BigDecimal("29.99"), null, 70, "Shirts",
								Arrays.asList("https://images.pexels.com/photos/2269872/pexels-photo-2269872.jpeg"),
								Arrays.asList("S", "M", "L"), Arrays.asList("Gray", "Blue"), "Gym", "Under Armour", 4.4,
								201),

						// ---------- HOODIES ----------
						createProductWithDetails("Casual Fleece Hoodie", "Warm fleece hoodie",
								new BigDecimal("49.99"), new BigDecimal("44.99"), 50, "Hoodie",
								Arrays.asList("https://images.pexels.com/photos/6311399/pexels-photo-6311399.jpeg"),
								Arrays.asList("M", "L", "XL"), Arrays.asList("Black", "Gray", "Navy"), "Casual",
								"Champion", 4.5, 267),

						createProductWithDetails("Gym Zip Hoodie", "Lightweight zip hoodie",
								new BigDecimal("54.99"), null, 45, "Hoodie",
								Arrays.asList("https://images.pexels.com/photos/7675413/pexels-photo-7675413.jpeg"),
								Arrays.asList("S", "M", "L"), Arrays.asList("Black", "Blue"), "Gym", "Adidas", 4.3,
								189),

						createProductWithDetails("Party Oversize Hoodie", "Trendy oversized hoodie",
								new BigDecimal("59.99"), new BigDecimal("49.99"), 35, "Hoodie",
								Arrays.asList("https://images.pexels.com/photos/631163/pexels-photo-631163.jpeg"),
								Arrays.asList("M", "L"), Arrays.asList("Red", "White"), "Party", "Supreme", 4.6, 345),

						createProductWithDetails("Formal Minimal Hoodie", "Minimal smart-casual hoodie",
								new BigDecimal("64.99"), null, 30, "Hoodie",
								Arrays.asList("https://images.pexels.com/photos/921265/pexels-photo-921265.jpeg"),
								Arrays.asList("L", "XL"), Arrays.asList("Gray", "Black"), "Formal", "Hugo Boss", 4.7,
								298),

						// ---------- JEANS ----------
						createProductWithDetails("Slim Fit Jeans", "Modern slim-fit stretch denim",
								new BigDecimal("49.99"), new BigDecimal("39.99"), 70, "Jeans",
								Arrays.asList("https://images.pexels.com/photos/2983462/pexels-photo-2983462.jpeg"),
								Arrays.asList("28", "30", "32", "34", "36"), Arrays.asList("Blue", "Black"), "Casual",
								"Levi's", 4.5, 456),

						createProductWithDetails("Formal Dark Jeans", "Dark wash formal denim",
								new BigDecimal("59.99"), null, 50, "Jeans",
								Arrays.asList("https://images.pexels.com/photos/298863/pexels-photo-298863.jpeg"),
								Arrays.asList("30", "32", "34", "36"), Arrays.asList("Dark Blue"), "Formal", "Wrangler",
								4.4, 312),

						createProductWithDetails("Ripped Party Jeans", "Ripped fashionable jeans",
								new BigDecimal("54.99"), new BigDecimal("44.99"), 45, "Jeans",
								Arrays.asList("https://images.pexels.com/photos/2529144/pexels-photo-2529144.jpeg"),
								Arrays.asList("28", "30", "32", "34"), Arrays.asList("Blue", "Black"), "Party",
								"Diesel", 4.2, 267),

						createProductWithDetails("Gym Stretch Jeans", "Flexible stretch denim for active wear",
								new BigDecimal("52.99"), null, 40, "Jeans",
								Arrays.asList("https://images.pexels.com/photos/532556/pexels-photo-532556.jpeg"),
								Arrays.asList("30", "32", "34", "36"), Arrays.asList("Black", "Gray"), "Gym", "Lee",
								4.3, 198));

				productRepository.saveAll(products);
				System.out.println("✅ Product Service: 28 enhanced products loaded into PostgreSQL.");
			}
		};
	}

	private Product createProductWithDetails(String name, String description, BigDecimal price,
			BigDecimal discountPrice, int stock, String category, List<String> imageUrls,
			List<String> sizes, List<String> colors, String dressStyle, String brand,
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
