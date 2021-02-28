package com.twiceyuan.easydi

import kotlin.properties.ReadOnlyProperty

/**
 * 一个简单的依赖注入工具实现。API 受 Koin 启发，但实现更加简单
 */
object EasyDI {

    /**
     * 存储所有 scope
     */
    private val scopeRegistry = mutableMapOf<ScopeID, Scope>()

    /**
     * scope 构造器定义
     */
    private val scopeDefinitions = mutableMapOf<Qualifier, ScopeDefinition>()

    /**
     * 默认 scope
     */
    val defaultScope: Scope = ScopeImpl()

    /**
     * 使用默认 Scope 进行定义
     */
    fun define(definition: Scope.() -> Unit) {
        defaultScope.definition()
    }

    /**
     * 定义 scope
     */
    fun defineScope(qualifier: Qualifier, definition: ScopeDefinition) {
        val existDef = scopeDefinitions[qualifier]
        if (existDef != null) {
            throw IllegalStateException("Scope $qualifier 已经定义过了")
        }
        scopeDefinitions[qualifier] = definition
    }

    /**
     * 创建或获取 scope
     */
    fun getOrCreateScope(
        scopeID: ScopeID,
        qualifier: Qualifier,
        properties: Map<String, Any> = emptyMap()
    ): Scope = synchronized(scopeID) {
        // scope 已经被创建的情况
        if (scopeRegistry.containsKey(scopeID)) {
            return scopeRegistry[scopeID]!!
        }

        val scopeDefinition: ScopeDefinition =
            scopeDefinitions[qualifier] ?: throw IllegalStateException("Scope $qualifier 还没有定义")

        val scope = ScopeImpl()

        scope.properties.putAll(properties)
        scope.scopeDefinition()

        this.scopeRegistry[scopeID] = scope

        return scope
    }

    /**
     * 获取 scope
     */
    fun getScope(scopeID: ScopeID): Scope = synchronized(scopeID) {
        return scopeRegistry[scopeID]
            ?: throw IllegalStateException("没有找到 scopeID 为 $scopeID 的 scope，请先创建")
    }

    /**
     * 关闭所有 scope
     */
    fun closeAll() {
        scopeRegistry.values.forEach { it.close() }
        scopeRegistry.clear()
    }
}

/**
 * Scope ID，其实就是字符串
 */
typealias ScopeID = String

/**
 * 定义的对象提供者
 */
sealed class Provider<T> {
    abstract fun provide(): T
}

/**
 * Single 对象提供者，只会运行一次
 */
class SingleProvider<T>(val provider: () -> T) : Provider<T>() {
    private val singleObject by lazy { provider() }
    override fun provide(): T {
        return singleObject
    }
}

/**
 * Factory 对象提供者，每次都会执行
 */
class FactoryProvider<T>(val provider: () -> T) : Provider<T>() {
    override fun provide(): T {
        return provider()
    }
}

/**
 * Scope 里的对象池
 */
typealias ScopeRegistry = MutableMap<Qualifier, Provider<*>>

/**
 * 限定符
 */
sealed class Qualifier {
    abstract val qualifier: String
}

/**
 * String 限定符
 */
class StringQualifier(override val qualifier: String) : Qualifier() {

    override fun equals(other: Any?): Boolean {
        return other is StringQualifier && qualifier == other.qualifier
    }

    override fun hashCode(): Int {
        return qualifier.hashCode()
    }
}

/**
 * 类型限定符
 */
class TypeQualifier<T>(private val type: Class<T>) : Qualifier() {

    override val qualifier: String
        get() = type.canonicalName!!

    override fun equals(other: Any?): Boolean {
        return other is TypeQualifier<*> && qualifier == other.qualifier
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}

/**
 * 创建限定符的两种方式：字符创方式
 */
fun named(qualifier: String) = StringQualifier(qualifier)

/**
 * 创建限定符的两种方式：类型参数方式
 */
inline fun <reified T> named() = TypeQualifier(T::class.java)

/**
 * Scope 定义
 */
interface Scope {
    val scopeRegistry: ScopeRegistry

    /**
     * scope 维护一个属性池
     */
    val properties: MutableMap<String, Any>

    fun close()
}


/**
 * Scope 实现
 */
class ScopeImpl: Scope {
    override val scopeRegistry: ScopeRegistry = mutableMapOf()

    /**
     * scope 维护一个属性池
     */
    override val properties = mutableMapOf<String, Any>()

    override fun close() {
        properties.clear()
        scopeRegistry.clear()
    }
}

/**
 * 设置注入的对象
 */
inline fun <reified T : Any> Scope.single(qualifier: Qualifier, noinline provider: () -> T) {
    scopeRegistry[qualifier] = SingleProvider(provider)
}

inline fun <reified T : Any> Scope.single(noinline provider: () -> T) {
    scopeRegistry[named<T>()] = SingleProvider(provider)
}

inline fun <reified T : Any> Scope.factory(
    qualifier: Qualifier,
    noinline provider: () -> T
) {
    scopeRegistry[qualifier] = FactoryProvider(provider)
}

inline fun <reified T : Any> Scope.factory(noinline provider: () -> T) {
    scopeRegistry[named<T>()] = FactoryProvider(provider)
}

/**
 * 获取注入的一个对象
 */
inline fun <reified T : Any> Scope.get(qualifier: Qualifier): T {
    val scope = this
    @Suppress("UNCHECKED_CAST")
    return (scope.scopeRegistry[qualifier] as Provider<T>).provide()
}

/**
 * 类型推导出 TypeQualifier 的 get 函数
 */
inline fun <reified T : Any> Scope.get(): T {
    val scope = this
    @Suppress("UNCHECKED_CAST")
    return (scope.scopeRegistry[named<T>()] as Provider<T>).provide()
}

/**
 * 获取注入的一个对象
 */
inline fun <reified T : Any> Scope.inject(qualifier: Qualifier): ReadOnlyProperty<Any, T> {
    val scope = this
    val provider = scope.scopeRegistry[qualifier]
    return ReadOnlyProperty { _, _ ->
        @Suppress("UNCHECKED_CAST")
        (provider as Provider<T>).provide()
    }
}

/**
 * 类型推导出 TypeQualifier 的 get 函数
 */
inline fun <reified T : Any> Scope.inject(): ReadOnlyProperty<Any, T> {
    val scope = this
    val provider = scope.scopeRegistry[named<T>()]
    return ReadOnlyProperty { _, _ ->
        @Suppress("UNCHECKED_CAST")
        (provider as Provider<T>).provide()
    }
}

inline fun <reified T : Any> get(qualifier: Qualifier): T = EasyDI.defaultScope.get(qualifier)

inline fun <reified T : Any> get(): T = EasyDI.defaultScope.get()

inline fun <reified T : Any> inject(qualifier: Qualifier): ReadOnlyProperty<*, T> =
    EasyDI.defaultScope.inject(qualifier)

inline fun <reified T : Any> inject(): ReadOnlyProperty<Any, T> =
    EasyDI.defaultScope.inject()

// Scope 的定义
typealias ScopeDefinition = Scope.() -> Unit
