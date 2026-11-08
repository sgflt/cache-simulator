package cz.zcu.kiv.cachesimulator

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** slf4j logger named after the class [T]. */
inline fun <reified T : Any> logger(): Logger = LoggerFactory.getLogger(T::class.java)
