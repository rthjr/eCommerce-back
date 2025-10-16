# Product API - Complete Specification with Active Field

## UPDATE PRODUCT (with Active Field)

**PUT** `/api/products/{id}`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**Request Body:**
```json
{
  "name": "Wireless Headphones",
  "description": "Updated description",
  "price": 89.99,
  "stockQuantity": 45,
  "category": "Electronics",
  "imageUrl": "https://example.com/headphones-new.jpg",
  "active": false
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Wireless Headphones",
  "description": "Updated description",
  "price": 89.99,
  "stockQuantity": 45,
  "category": "Electronics",
  "sellerId": "507f1f77bcf86cd799439011",
  "sellerName": "John Doe",
  "imageUrl": "https://example.com/headphones-new.jpg",
  "active": false,
  "brand": "Sony",
  "rating": 4.5,
  "numReviews": 120,
  "discountPrice": 79.99,
  "imageUrls": ["https://example.com/headphones-new.jpg"],
  "sizes": null,
  "colors": ["Black", "White"],
  "dressStyle": null,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T12:00:00"
}
```

**Response (404 Not Found):** Product not found

---

## Frontend Implementation

### Update Product with Active Status
```javascript
const updateProduct = async (productId, productData) => {
  const token = localStorage.getItem('accessToken');
  const user = JSON.parse(localStorage.getItem('userInfo'));
  
  // Check ownership
  const product = await getProductById(productId);
  if (product.sellerId !== user.id) {
    throw new Error('You can only edit your own products');
  }
  
  const response = await fetch(
    `http://localhost:XXXX/api/products/${productId}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        name: productData.name,
        description: productData.description,
        price: productData.price,
        stockQuantity: productData.stockQuantity,
        category: productData.category,
        imageUrl: productData.imageUrl,
        active: productData.active  // Can be true or false
      })
    }
  );
  
  if (response.status === 403) {
    throw new Error('Unauthorized: Not product owner');
  }
  
  return await response.json();
};
```

### Toggle Active Status (Simple Update)
```javascript
const toggleProductActive = async (productId, currentStatus) => {
  const token = localStorage.getItem('accessToken');
  
  // Get current product data
  const product = await getProductById(productId);
  
  // Update with toggled active status
  const response = await fetch(
    `http://localhost:XXXX/api/products/${productId}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        name: product.name,
        description: product.description,
        price: product.price,
        stockQuantity: product.stockQuantity,
        category: product.category,
        imageUrl: product.imageUrl,
        active: !currentStatus  // Toggle the status
      })
    }
  );
  
  return await response.json();
};
```

### Edit Product Form Component
```javascript
const EditProductForm = ({ productId }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: 0,
    stockQuantity: 0,
    category: '',
    imageUrl: '',
    active: true
  });
  
  useEffect(() => {
    const fetchProduct = async () => {
      const data = await getProductById(productId);
      
      if (!isProductOwner(data)) {
        alert('You can only edit your own products');
        navigate('/products');
        return;
      }
      
      setFormData({
        name: data.name,
        description: data.description,
        price: data.price,
        stockQuantity: data.stockQuantity,
        category: data.category,
        imageUrl: data.imageUrl,
        active: data.active
      });
    };
    
    fetchProduct();
  }, [productId]);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      await updateProduct(productId, formData);
      alert('Product updated successfully!');
      navigate(`/products/${productId}`);
    } catch (error) {
      alert('Error updating product: ' + error.message);
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Product Name"
        value={formData.name}
        onChange={(e) => setFormData({...formData, name: e.target.value})}
        required
      />
      <textarea
        placeholder="Description"
        value={formData.description}
        onChange={(e) => setFormData({...formData, description: e.target.value})}
        required
      />
      <input
        type="number"
        placeholder="Price"
        value={formData.price}
        onChange={(e) => setFormData({...formData, price: parseFloat(e.target.value)})}
        required
      />
      <input
        type="number"
        placeholder="Stock Quantity"
        value={formData.stockQuantity}
        onChange={(e) => setFormData({...formData, stockQuantity: parseInt(e.target.value)})}
        required
      />
      <input
        type="text"
        placeholder="Category"
        value={formData.category}
        onChange={(e) => setFormData({...formData, category: e.target.value})}
        required
      />
      <input
        type="url"
        placeholder="Image URL"
        value={formData.imageUrl}
        onChange={(e) => setFormData({...formData, imageUrl: e.target.value})}
        required
      />
      
      {/* Active Status Toggle */}
      <label>
        <input
          type="checkbox"
          checked={formData.active}
          onChange={(e) => setFormData({...formData, active: e.target.checked})}
        />
        Active (visible to customers)
      </label>
      
      <button type="submit">Update Product</button>
    </form>
  );
};
```

### Product Card with Status Indicator
```javascript
const ProductCard = ({ product }) => {
  const user = getCurrentUserFromStorage();
  const isOwner = user && product.sellerId === user.id;
  
  const handleToggleStatus = async () => {
    try {
      const updated = await toggleProductActive(product.id, product.active);
      alert(`Product ${updated.active ? 'activated' : 'deactivated'} successfully!`);
      // Refresh product list
    } catch (error) {
      alert('Error toggling status: ' + error.message);
    }
  };
  
  return (
    <div className={`product-card ${!product.active ? 'inactive' : ''}`}>
      <img src={product.imageUrl} alt={product.name} />
      <h3>{product.name}</h3>
      <p>{product.description}</p>
      <p className="price">${product.price}</p>
      <p className="seller">Sold by: {product.sellerName}</p>
      
      {/* Status Badge */}
      <span className={`status-badge ${product.active ? 'active' : 'inactive'}`}>
        {product.active ? 'Active' : 'Inactive'}
      </span>
      
      {isOwner && (
        <div className="owner-actions">
          <button onClick={() => navigate(`/products/${product.id}/edit`)}>
            Edit
          </button>
          <button onClick={handleToggleStatus}>
            {product.active ? 'Deactivate' : 'Activate'}
          </button>
          <button onClick={() => handleDelete(product.id)}>
            Delete
          </button>
        </div>
      )}
      
      {!isOwner && product.active && (
        <button onClick={() => handleAddToCart(product)}>
          Add to Cart
        </button>
      )}
      
      {!product.active && !isOwner && (
        <p className="unavailable">This product is currently unavailable</p>
      )}
    </div>
  );
};
```

---

## Summary of Changes

### Backend Changes:
1. ✅ Added `active` field to `ProductRequest` DTO
2. ✅ Updated `updateProductFromRequest()` method to handle `active` field
3. ✅ Active field is optional in update request (only updates if provided)

### Frontend Usage:
1. ✅ Can update product active status via PUT request
2. ✅ Can toggle active/inactive in edit form
3. ✅ Can show status badge on product cards
4. ✅ Can hide inactive products from non-owners
5. ✅ Can provide quick toggle button for owners

### API Behavior:
- If `active` is not provided in request, it keeps current value
- If `active: true` is provided, product becomes visible
- If `active: false` is provided, product becomes hidden (soft delete)
- DELETE endpoint still sets `active: false` (soft delete)
