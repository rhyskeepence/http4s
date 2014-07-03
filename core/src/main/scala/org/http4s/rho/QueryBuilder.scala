package org.http4s
package rho

import scala.language.existentials

import org.http4s.rho.bits.{MetaDataSyntax, Metadata, HListToFunc, HeaderAppendable}
import bits.PathAST._
import bits.QueryAST._
import bits.HeaderAST._
import bits.QueryAST.MetaCons

import shapeless.ops.hlist.Prepend

import shapeless.{::, HList}


case class QueryBuilder[T <: HList](method: Method,
                        path: PathRule[_ <: HList],
                        query: QueryRule[_ <: HList])
      extends RouteExecutable[T]
      with HeaderAppendable[T]
      with MetaDataSyntax
{
  override type Self = QueryBuilder[T]

  override def makeAction[F](f: F, hf: HListToFunc[T, F]): RhoAction[T, F] =
    RhoAction(Router(method, path, query, validators), f, hf)

  override def >>>[T1 <: HList](v: HeaderRule[T1])(implicit prep1: Prepend[T1, T]): Router[prep1.Out] =
    Router(method, path, query, v)

  override def addMetaData(data: Metadata): Self = QueryBuilder(method, path, MetaCons(query, data))

  def &[T1 <: HList](rule: QueryRule[T1])(implicit prep: Prepend[T1, T]): QueryBuilder[prep.Out] =
    QueryBuilder(method, path, QueryAnd(query, rule))

  override def validators: HeaderRule[_ <: HList] = EmptyHeaderRule
}
