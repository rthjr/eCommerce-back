# Product-Owner Relationship Endpoints

## Implemented Changes

### 1. Product Model
- ✅ Changed `sellerId` from `Long` to `String` (matches User.id from MongoDB)

### 2. Repository (ProductRepository.java)
- ✅ Added `findBySellerIdAndActiveTrue(String sellerId)` method

### 3. Service (ProductService.java)
- ✅ Added `getProductsBySellerId(String sellerId)` method
- ✅ Added TODO comments for JWT extraction and ownership validation

### 4. Controller (ProductController.java)
- ✅ Modified `GET /api/products` to accept optional `sellerId` query parameter
- ✅ Added `GET /api/products/seller/{sellerId}` endpoint

---

## API Endpoints

### Get All Products (with optional seller filter)
```
GET /api/products
GET /api/products?sellerId={sellerId}
```
**Response:** List of ProductResponse

### Get Products by Seller (alternative endpoint)
```
GET /api/products/seller/{sellerId}
```
**Response:** List of ProductResponse

### Create Product
```
POST /api/products
Headers: Authorization: Bearer {token}
Body: ProductRequest
```
**Status:** 201 Created
**Response:** ProductResponse
**Note:** Backend should extract sellerId and sellerName from JWT

### Update Product
```
PUT /api/products/{id}
Headers: Authorization: Bearer {token}
Body: ProductRequest
```
**Status:** 200 OK or 404 Not Found
**Response:** ProductResponse
**Note:** Backend should validate ownership before update

### Delete Product (Soft Delete)
```
DELETE /api/products/{id}
Headers: Authorization: Bearer {token}
```
**Status:** 204 No Content or 404 Not Found
**Note:** Backend should validate ownership before delete

### Get Single Product
```
GET /api/products/{id}
```
**Status:** 200 OK or 404 Not Found
**Response:** ProductResponse

---

## TODO: Backend Implementation Needed

### 1. JWT Token Extraction in Controller
Add to ProductController:
```java
@PostMapping
public ResponseEntity<ProductResponse> createProduct(
        @RequestBody ProductRequest productRequest,
        @RequestHeader("Authorization") String token) {
    
    // Extract user info from JWT
    String userId = jwtTokenProvider.getUserIdFromJWT(token.substring(7));
    String userEmail = jwtTokenProvider.getEmailFromJWT(token.substring(7));
    
    ProductResponse response = productService.createProduct(productRequest, userId, userEmail);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### 2. Ownership Validation
Add to ProductController before update/delete:
```java
@PutMapping("/{id}")
public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long id,
        @RequestBody ProductRequest productRequest,
        @RequestHeader("Authorization") String token) {
    
    String userId = jwtTokenProvider.getUserIdFromJWT(token.substring(7));
    
    // Validate ownership
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    
    if (!product.getSellerId().equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    return productService.updateProduct(id, productRequest)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
}
```

### 3. Update Service Method Signature
```java
public ProductResponse createProduct(ProductRequest productRequest, String sellerId, String sellerName) {
    Product product = new Product();
    updateProductFromRequest(product, productRequest);
    product.setSellerId(sellerId);
    product.setSellerName(sellerName);
    Product savedProduct = productRepository.save(product);
    return mapToProductResponse(savedProduct);
}
```

---

## Frontend Usage Examples

### Get Current User's Products
```javascript
const currentUser = JSON.parse(localStorage.getItem('userInfo'));
const response = await fetch(`/api/products?sellerId=${currentUser.id}`);
// OR
const response = await fetch(`/api/products/seller/${currentUser.id}`);
```

### Create Product with Auth
```javascript
const token = localStorage.getItem('accessToken');
const response = await fetch('/api/products', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    name: 'Product Name',
    description: 'Description',
    price: 99.99,
    stockQuantity: 100,
    category: 'Electronics',
    imageUrl: 'https://example.com/image.jpg'
  })
});
```

### Check Ownership Before Edit
```javascript
const currentUser = JSON.parse(localStorage.getItem('userInfo'));
const isOwner = product.sellerId === currentUser.id;

if (isOwner) {
  // Show edit/delete buttons
}
```

### Update Product
```javascript
if (product.sellerId !== currentUser.id) {
  alert('You can only edit your own products');
  return;
}

const token = localStorage.getItem('accessToken');
const response = await fetch(`/api/products/${product.id}`, {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify(updatedProduct)
});
```

### Delete Product
```javascript
if (product.sellerId !== currentUser.id) {
  alert('You can only delete your own products');
  return;
}

const token = localStorage.getItem('accessToken');
const response = await fetch(`/api/products/${product.id}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

---

## Summary

✅ **Completed:**
- Data type alignment (sellerId is now String)
- Repository method for seller filtering
- Service method for seller-based queries
- Controller endpoints for filtering by seller

⚠️ **Pending (Backend):**
- JWT token extraction in create endpoint
- Auto-populate sellerId and sellerName from JWT
- Ownership validation in update/delete endpoints
- Return 403 Forbidden for unauthorized access

✅ **Frontend Ready:**
- Can filter products by sellerId
- Can check ownership client-side
- Can send Authorization headers
- Can handle ownership-based UI rendering
