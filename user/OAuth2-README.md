# OAuth2 Implementation

This document describes the OAuth2 implementation for the user service.

## Overview

The OAuth2 implementation allows users to authenticate using external OAuth2 providers (Google, GitHub, Facebook) while maintaining the existing JWT authentication system.

## Architecture

### Components

1. **OAuth2Config** - Configuration properties for OAuth2 providers
2. **OAuth2UserInfo** - Entity to store OAuth2 user information
3. **OAuth2Token** - Entity to store OAuth2 access tokens
4. **OAuth2ClientService** - Service to handle OAuth2 user processing
5. **CustomOAuth2UserService** - Custom OAuth2 user service
6. **OAuth2AuthenticationSuccessHandler** - Handles successful OAuth2 authentication
7. **OAuth2Controller** - REST endpoints for OAuth2 operations
8. **OAuth2SecurityConfig** - Security configuration for OAuth2

### Database Schema

#### oauth2_user_info Collection
```javascript
{
  "_id": "507f1f77bcf86cd799439011",
  "user_id": "507f1f77bcf86cd799439012",
  "provider": "GOOGLE",
  "provider_id": "123456789",
  "email": "user@example.com",
  "name": "John Doe",
  "avatar_url": "https://example.com/avatar.jpg",
  "attributes": {
    "sub": "123456789",
    "email": "user@example.com",
    "name": "John Doe",
    "picture": "https://example.com/avatar.jpg"
  },
  "created_at": "2024-01-08T10:30:00"
}
```

#### oauth2_tokens Collection
```javascript
{
  "_id": "507f1f77bcf86cd799439013",
  "user_id": "507f1f77bcf86cd799439012",
  "provider": "GOOGLE",
  "access_token": "ya29.a0AfH6SMC...",
  "refresh_token": "1//0gX...",
  "token_type": "Bearer",
  "expires_at": "2024-01-08T11:30:00Z",
  "scope": "email profile",
  "created_at": "2024-01-08T10:30:00"
}
```

## Configuration

### Environment Variables

Set the following environment variables:

```bash
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8082/oauth2/callback/google

# GitHub OAuth2
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
GITHUB_REDIRECT_URI=http://localhost:8082/oauth2/callback/github

# Facebook OAuth2
FACEBOOK_CLIENT_ID=your-facebook-client-id
FACEBOOK_CLIENT_SECRET=your-facebook-client-secret
FACEBOOK_REDIRECT_URI=http://localhost:8082/oauth2/callback/facebook
```

### OAuth2 Provider Setup

#### Google
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Create OAuth2 credentials
5. Add authorized redirect URI: `http://localhost:8082/oauth2/callback/google`

#### GitHub
1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Create a new OAuth App
3. Set Authorization callback URL: `http://localhost:8082/oauth2/callback/github`

#### Facebook
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app
3. Add Facebook Login product
4. Set Valid OAuth Redirect URIs: `http://localhost:8082/oauth2/callback/facebook`

## API Endpoints

### OAuth2 Authentication

#### Get Available Providers
```
GET /api/oauth2/providers
```

#### OAuth2 Login URLs
```
GET /oauth2/authorization/google
GET /oauth2/authorization/github
GET /oauth2/authorization/facebook
```

#### OAuth2 Callback
```
GET /oauth2/callback/{provider}
```

#### Get OAuth2 User Info
```
GET /api/oauth2/user
Authorization: Bearer {jwt_token}
```

#### Link OAuth2 Account
```
POST /api/oauth2/link/{provider}
Authorization: Bearer {jwt_token}
```

#### Unlink OAuth2 Account
```
POST /api/oauth2/unlink/{provider}
Authorization: Bearer {jwt_token}
```

## Authentication Flow

1. User clicks on OAuth2 login button (e.g., "Login with Google")
2. User is redirected to OAuth2 provider's authorization page
3. User grants permission to the application
4. OAuth2 provider redirects back to the application with authorization code
5. Application exchanges authorization code for access token
6. Application fetches user information from OAuth2 provider
7. Application processes user information:
   - Creates new user if not exists
   - Links OAuth2 account to existing user if email matches
   - Stores OAuth2 user info and tokens
8. Application generates JWT token for the user
9. User is redirected to frontend with JWT token
10. Frontend stores JWT token and authenticates user

## Security Features

- **JWT Integration**: OAuth2 authentication generates JWT tokens for seamless integration
- **Account Linking**: OAuth2 accounts can be linked to existing email/password accounts
- **Token Storage**: OAuth2 access tokens are stored for future API calls
- **User Data Protection**: OAuth2 user attributes are stored securely in MongoDB
- **Provider Validation**: Only configured OAuth2 providers are allowed

## Error Handling

### OAuth2AuthenticationException
Thrown when OAuth2 authentication fails.

### Common Error Scenarios
- Invalid OAuth2 client credentials
- User denies authorization
- OAuth2 provider API errors
- Network connectivity issues
- Invalid redirect URI

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -P integration-test
```

## Troubleshooting

### Common Issues

1. **Invalid Client Credentials**
   - Check environment variables
   - Verify OAuth2 app configuration

2. **Redirect URI Mismatch**
   - Ensure redirect URI matches OAuth2 app configuration
   - Check for trailing slashes

3. **User Not Found**
   - Check OAuth2 user info extraction logic
   - Verify provider-specific attribute names

4. **JWT Token Generation Errors**
   - Check JWT configuration
   - Verify user creation logic

## Future Enhancements

1. **Additional OAuth2 Providers**: Add support for more providers (LinkedIn, Twitter, etc.)
2. **Account Merging**: Allow users to merge multiple OAuth2 accounts
3. **Token Refresh**: Implement automatic OAuth2 token refresh
4. **Social Profile Management**: Enhanced user profile management
5. **Multi-tenant Support**: Support for multiple OAuth2 applications
