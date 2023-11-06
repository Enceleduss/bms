# Brief Introduction

This is frontend implementation of my use case app in react using vite. A strong and secure authentication mechanism was followed oauth2.0 refresh token based.

## All the features are listed below

- Login
- Register
- User Details Page
- Deposit Functionality
- Apply Loan
- Loan Details
- Logout

## Login and Authentication

User id and password based login. Integrated with backend using axios. Once authentication is done, a jwt token is (Access Token) and a jwt token(Refresh Token) issued by server. Access token is stored using redux state. Access token will be sent to server for subsequent requests which facilitates authentication in a stateless protocol.
Expiry for access token is kept very short ~ 1 minute. Once access token is expired, the axios istance's interceptor senses it and requests a new access token using the refresh token. Refresh token's expiry is kept 5 minute -- In practical scenario we can keep it few days/months.

## Register

All the fields are mandatory and validated before the form submission. User is prompted with help messages whereever needed.

## User Details Page

A simple display only table with all fields.

## Deposit

A modal view on user details page, to update the account balance.

## Apply Loan

A dynamic form where fields are populated depending on the loan type selected by user. Make a post request to send form data to server.

## Loan Details

All card view that displays all the loan applications in a grid.

## Logout

Logout was achieved by invalidating the refresh token on server side.
