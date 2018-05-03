package org.biobank.fixtures

object IdGenerator {
  private val n = new java.util.concurrent.atomic.AtomicLong

  def next = n.getAndIncrement()
}
