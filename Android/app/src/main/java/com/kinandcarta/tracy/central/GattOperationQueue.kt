package com.kinandcarta.tracy.central

import android.os.Handler
import android.os.Looper

/**
 * When working with Bluetooth, you can only run one Bluetooth operation at once, including connecting.
 *
 * This means that you have to queue the operations until a callback has been received. We'll handle
 * that here by creating a simple queue of operations.
 *
 * When you want to perform an operation, add the block to the queue. If the queue is empty when you
 * add the operation, it'll be invoked immediately, otherwise, it'll be added to the end of the queue.
 *
 * You are repsonsible for letting the queue know when the last operation has been completed by calling
 * `GattOperationQueue#pop`. When popping an operation, it removes the first operation in the queue and
 * then immediately executes the next one in the queue (if any).
 *
 * Additionally, since Bluetooth callbacks are called on a Binder thread, this OperationQueue will force
 * all operations to be executed on the main thread, which can fix some issues on some hardware and software versions.
 */
class GattOperationQueue {

    private val operations = mutableListOf<() -> Unit>()

    /**
     * Pops the last executed operation off the queue and executes the next one (if any).
     */
    fun pop() {
        if (operations.isEmpty()) return
        operations.removeAt(0)
        performNextIfAny()
    }

    /**
     * Adds an operation to the queue.
     * If the queue is empty when adding it will be executed immediately.
     * You must call `pop()` when you receive a callback to say that this operation is complete.
     */
    fun push(operation: () -> Unit) {
        operations.add(operation)
        if (operations.size == 1) performNextIfAny()
    }

    /**
     * Clears all operations from the queue immediately.
     */
    fun clear() {
        operations.clear()
    }

    private fun performNextIfAny() {
        val nextOperation = operations.firstOrNull() ?: return
        val handler = Handler(Looper.getMainLooper())
        handler.post(nextOperation)
    }

}