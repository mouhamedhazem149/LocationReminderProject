/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

enum class AuthenticationState {
    AUTHENTICATED, UNAUTHENTICATED
}
// livedata to listen to changes in auth state
class UserLiveData : LiveData<AuthenticationState>() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser != null) {
            value = AuthenticationState.AUTHENTICATED
        } else {
            value = AuthenticationState.UNAUTHENTICATED
        }
    }

    fun getUser() : FirebaseUser?{
        return firebaseAuth.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}