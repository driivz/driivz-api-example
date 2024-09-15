# Driivz API integration example

**Contents**
1. [Overview](#overview)
2. [Setup](#setup)
3. [Demo](#demo)

## Overview

This sample app demonstrates integrating with [Driivz APIs](https://docs.driivz.com/) using Android compose components app and a ktor server that this Android app is communicating with.
The app is communicating with the server written in ktor framework to perform driver login, 
list the payment methods that this driver has, add a payment method to the logged in driver (currently only Stripe is available in this demo), show sites on a map,
fetch chargers that are associated to a particular site and initiate an OTP transaction with a selected charger.

___

__Note about Stripe integration:__ The Stripe integration includes only a simple credit card component that can create a token for a SetupIntent
only with card number, expiration & cvv inputs. 3DS or other 
Stripe integrations are not available in this example. In this example it is merely shown how to retrieve a token and save it as an
authorized token within Driivz system.
___

### APIs

This integration is powered by the following Driivz APIs
* [Operator login API](https://docs.driivz.com/apidocs/operator-login-1?highlight=v1authenticationoperatorlogin)
* [Find accounts API](https://docs.driivz.com/apidocs/find-by-account-customer-1?highlight=v1customer-accountsfilter)
* [Operator login as customer API](https://docs.driivz.com/apidocs/operator-login-as-customer-1?highlight=v1authenticationoperatorcustomer-login)
* [Get card payment method details API](https://docs.driivz.com/apidocs/get-card-payment-methods-by-account-number-1?highlight=v1accounts%7BaccountNumber%7Dpayment-methodspayment-cards)
* [Add card payment method API](https://docs.driivz.com/apidocs/add-card-payment-method-1)
* [Find site API](https://docs.driivz.com/apidocs/find-by-id-26)
* [Get charger’s profile details API](https://docs.driivz.com/apidocs/find-by-id-20)
* [Search sites API](https://docs.driivz.com/apidocs/search-sites)
* [Find charger locations](https://docs.driivz.com/apidocs/find-charger-locations-details-by-filter-1?highlight=v1chargerslocationsfilter)
* [Send guest driver start transaction command](https://docs.driivz.com/apidocs/send-start-transaction-message?highlight=remote-operationsone-time-payment-start-transaction)

### App components
The Android app is comprised of six screens that are composable functions:
1. [MainScreen](https://github.com/driivz/driivz-api-example/blob/main/composeApp/src/androidMain/kotlin/com/driivz/example/screen/MainScreen.kt), which represents the main hub to let the user choose a particular flow, either add payment method to logged in driver or initiate an OTP charging session using his payment method details

  <img width="246" height="506" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/screenshots/screenshot01.png" />

2. [LoginScreen](https://github.com/driivz/driivz-api-example/blob/main/composeApp/src/androidMain/kotlin/com/driivz/example/screen/LoginScreen.kt), which represents the driver login credentials screen

   <img width="246" height="506" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/screenshots/screenshot02.png" />

3. [PaymentListScreen](https://github.com/driivz/driivz-api-example/blob/main/composeApp/src/androidMain/kotlin/com/driivz/example/screen/PaymentListScreen.kt), which represents all the payment methods that the logged in driver has

   <img width="246" height="506" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/screenshots/screenshot03.png" />

4. [MapScreen](https://github.com/driivz/driivz-api-example/blob/main/composeApp/src/androidMain/kotlin/com/driivz/example/screen/MapScreen.kt), which represents a map with site pins or cluster of site pins on this map

    <img width="246" height="506" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/screenshots/screenshot04.png" />

5. [ChargerListScreen](https://github.com/driivz/driivz-api-example/blob/main/composeApp/src/androidMain/kotlin/com/driivz/example/screen/ChargerListScreen.kt), which represents a list of chargers in a particular site

    <img width="246" height="506" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/screenshots/screenshot05.png" />

6. [PaymentScreen](https://github.com/driivz/driivz-api-example/blob/main/composeApp/src/androidMain/kotlin/com/driivz/example/screen/PaymentScreen.kt), which represents a payment component to either create a payment for a driver or one time payment for a charging session

    <img width="246" height="506" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/screenshots/screenshot06.png" />


## Setup

### Install
1. Clone the `driivz-api-example` repository.
2. Open the project in Android Studio.
3. After [configuring the apps](#configure-the-apps), build and [run the server app](#run-the-server-app) and the android app separately.

<img width="215" height="108" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/run.png" />

### Configure the apps

#### Required
Edit the server configuration file [application.conf](https://github.com/driivz/driivz-api-example/blob/main/server/src/main/resources/application.conf)

1. Set the `ktor.serviceAccount.baseURL`
   to the api-gateway server URL that you'd like to connect to.

   For example,
   ```
   ktor {
       serviceAccount {
           baseURL = "https://example.driivz.com/api-gateway/"
       }
   }
   ```

2. Set the `ktor.serviceAccount.userName` & `ktor.serviceAccount.password`
   to the operator user that is going to be used to authenticate all the operations with Driivz apis (the service account).

   For example,
   ```
   ktor {
       serviceAccount {
           userName = "example@driivz.com"
           password = "Pass123"
       }
   }
   ```

3. Set the `ktor.stripe.privateKey`
   to the [Stripe account private key](https://dashboard.stripe.com/test/apikeys) that will be used to create the token and Stripe payment integration. 

   For example,
   ```
   ktor {
       stripe {
           privateKey = "sk_test_12345"
       }
   }
   ```

4. Set the `ktor.stripe.publicKey`
   to the [Stripe account private key](https://dashboard.stripe.com/test/apikeys) that will be used to create the token and Stripe payment integration. 

   For example,
   ```
   ktor {
       stripe {
           publicKey = "pk_test_12345"
       }
   }
   ```

Edit the run configuration of `ApplicationKt`

1. Set the VM option `config.file` to the application.conf file location

   For example,
   ```
   -Dconfig.file=/local-machine-path/driivz-api-example/server/src/main/resources/application.conf
   ```

Edit the local properties file `local.properties`

1. Add a property `MAPS_API_KEY` with the key from your google maps API console.
   You need this key for the map to show

   For example,
   ```
   MAPS_API_KEY=key_from_google_apis_console
   ```

#### Optional

Edit the android app urls XML file [urls.xml](https://github.com/driivz/driivz-api-example/blob/main/composeApp/src/androidMain/res/values/urls.xml)

1. Set the string resource `base_url` to the URL of the ktor server.
   by default it is set to http://10.0.2.2:8090 which is local host from the android emulator.

   For example,
   ```
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="base_url">http://192.168.2.1:8090</string>
   </resources>
   ```

## Demo

The following is a demonstration of a Customer (driver) adding a payment
1. Login with driver credentials
2. Navigating to the driver's payment methods list
3. Click on "Add payment" and navigating to the payment screen
4. Fill test credit card details
5. Click on "Pay now"
6. App navigating back to the payment methods list
7. Payment method is added successfully to payment methods list

<img width="320" height="658" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/demo1.gif" />

The following is a demonstration of a Customer (driver) that is not authenticated paying with one time payment with a specific charger from a map of sites
1. Navigate to the map of sites clicking on the "Sites map" button
2. Clicking on the cluster of sites or on a particular site pin in the map
3. Navigating to the site's chargers list
4. Pick a charger from the list and navigating to the payment screen
5. Fill test credit card details
6. Click on "Pay now"
7. App navigating back to the main screen

<img width="320" height="658" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/demo2.gif" />

The following is a demonstration of a Customer (driver) that is not authenticated paying with one time payment with a specific charger from the charger ID input in main screen
1. Fill some charger ID in the charger ID input in the main screen
2. Clicking on the "Charger (OTP)" button
3. Navigating to the payment screen
4. Fill test credit card details
5. Click on "Pay now"
6. App navigating back to the main screen

<img width="320" height="658" src="https://raw.githubusercontent.com/driivz/driivz-api-example/main/assets/demo3.gif" />