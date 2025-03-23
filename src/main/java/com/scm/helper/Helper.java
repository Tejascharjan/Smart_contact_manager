package com.scm.helper;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class Helper {

    public static String getEmailOfLoggedInUser(Authentication authentication) {

        // if logged in with email and password

        if (authentication instanceof OAuth2AuthenticationToken) {

            var aOAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            String clientId = aOAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

            var oauth2User = (OAuth2User) authentication.getPrincipal();
            String username = "";

            if (clientId.equalsIgnoreCase("google")) {

                // or if sign in with google
                username = oauth2User.getAttribute("email").toString();

            } else if (clientId.equalsIgnoreCase("github")) {

                // of if sign in with github
                System.out.println("getting email from github");
                username = oauth2User.getAttribute("email") != null ? oauth2User.getAttribute("email")
                        : oauth2User.getAttribute("login") + "@gmail.com";
            }

            // sign with facebook
            return username;

        } else {
            System.out.println("getting data from local database");
            return authentication.getName();
        }
    }

    public static String getLinkForEmailVerification(String emailToken) {
        String link = "http://localhost:8080/auth/verify-email?token=" + emailToken;
        return link;
    }
}
