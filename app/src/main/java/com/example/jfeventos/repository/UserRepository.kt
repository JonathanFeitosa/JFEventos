package com.example.jfeventos.repository

interface UserRepository {
    fun setName(name: String)
    fun setEmail(email: String)
    fun getName() : String?
    fun getEmail() : String?
}