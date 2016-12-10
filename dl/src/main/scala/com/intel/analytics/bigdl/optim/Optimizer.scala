/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.bigdl.optim

import com.intel.analytics.bigdl.nn.{Criterion, Module}
import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.tensor.TensorNumericMath.TensorNumeric
import com.intel.analytics.bigdl.utils.{T, Activities, File, Table}
import com.intel.analytics.bigdl.dataset.{DataSet => DataSource}

import scala.reflect.ClassTag

abstract class Optimizer[T  : ClassTag, TDS, VDS](
    protected val model: Module[Activities, Activities, T],
    protected val dataset: DataSource[TDS],
    protected val criterion: Criterion[Activities, T])(implicit ev : TensorNumeric[T])
{
  protected var state: Table = T()
  protected var optimMethod: OptimMethod[T] = new SGD[T]()
  protected var endWhen: Trigger = Trigger.maxIteration(100)

  protected var cacheTrigger: Option[Trigger] = None
  protected var cachePath: Option[String] = None
  protected var isOverWrite: Boolean = false

  protected var validationTrigger: Option[Trigger] = None
  protected var validationMethods: Option[Array[ValidationMethod[T]]] = None
  protected var validationDataSet: Option[DataSource[VDS]] = None

  def optimize(): Module[Activities, Activities, T]

  def setValidation(trigger: Trigger, dataset: DataSource[VDS],
    vMethods : Array[ValidationMethod[T]])
  : this.type = {
    this.validationTrigger = Some(trigger)
    this.validationDataSet = Some(dataset)
    this.validationMethods = Some(vMethods)
    this
  }

  def setCache(path: String, trigger: Trigger): this.type = {
    this.cachePath = Some(path)
    this.cacheTrigger = Some(trigger)
    this
  }

  def overWriteCache() : this.type = {
    isOverWrite = true
    this
  }

  def setState(state: Table): this.type = {
    this.state = state
    this
  }

  def setOptimMethod(method : OptimMethod[T]): this.type = {
    this.optimMethod = method
    this
  }

  def setEndWhen(endWhen: Trigger): this.type = {
    this.endWhen = endWhen
    this
  }

  protected def saveModel(model: Module[Activities, Activities, T],
    postfix: String = ""): this.type = {
    if (this.cachePath.isDefined) {
      model.save(s"${cachePath.get}.model$postfix", isOverWrite)
    }
    this
  }

  protected def saveState(state: Table, postfix: String = ""): this.type = {
    if (this.cachePath.isDefined) {
      state.save(s"${cachePath.get}.state$postfix", isOverWrite)
    }
    this
  }

  protected def header(epoch: Int, count: Int, total: Long, iter: Int, wallClockTime: Long)
  : String = {
    s"[Epoch $epoch $count/$total][Iteration $iter][Wall Clock ${wallClockTime / 1e9}s]"
  }
}