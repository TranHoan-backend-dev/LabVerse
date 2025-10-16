package com.se1853_jv.service

import org.springframework.stereotype.Service
import java.util.*

@Service
class EncoderService {
    fun encode(str: String): String = Base64.getUrlEncoder().encodeToString(str.toByteArray())

    fun decode(str: String?): String = String(Base64.getUrlDecoder().decode(str))
}