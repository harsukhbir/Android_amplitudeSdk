package com.vybesxapp.service.domain_model

data class User(
    val id: String,
    val name: String?,
    val username: String?,
    val email: String,
    val phoneNumber: String?,
    val profileImage: String?,
    val slug: String?,
)