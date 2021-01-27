package dodona.lib.domain.dodona.market

import io.circe.Decoder
import io.circe.generic.semiauto._
import cats.syntax.functor._

sealed trait Filter
final case class PriceFilter(
    filterType: String,
    minPrice: BigDecimal,
    maxPrice: BigDecimal,
    tickSize: BigDecimal
) extends Filter
final case class PercentPrice(
    filterType: String,
    multiplierUp: Int,
    multiplierDown: BigDecimal,
    avgPriceMins: Int
) extends Filter
final case class LotSize(
    filterType: String,
    minQty: BigDecimal,
    maxQty: BigDecimal,
    stepSize: BigDecimal
) extends Filter
final case class MinNotational(
    filterType: String,
    minNotional: BigDecimal,
    applyToMarket: Boolean,
    avgPriceMins: Int
) extends Filter
final case class IcebergParts(
    filterType: String,
    limit: Int
) extends Filter
final case class MarketLotSize(
    filterType: String,
    minQty: BigDecimal,
    maxQty: BigDecimal,
    stepSize: BigDecimal
) extends Filter
final case class MaxNumOrders(
    filterType: String,
    maxNumOrders: Int
) extends Filter
final case class MaxNumAlgoOrders(
    filterType: String,
    maxNumAlgoOrders: Int
) extends Filter

object Filter {
  lazy implicit val FilterDecoder: Decoder[Filter] =
    List[Decoder[Filter]](
      Decoder[PriceFilter].widen,
      Decoder[PercentPrice].widen,
      Decoder[LotSize].widen,
      Decoder[MinNotational].widen,
      Decoder[IcebergParts].widen,
      Decoder[MarketLotSize].widen,
      Decoder[MaxNumOrders].widen,
      Decoder[MaxNumAlgoOrders].widen
    ).reduceLeft(_ or _)
}

object PriceFilter {
  lazy implicit val PriceFilterDecoder: Decoder[PriceFilter] = deriveDecoder
}

object PercentPrice {
  lazy implicit val PriceFilterDecoder: Decoder[PercentPrice] = deriveDecoder
}

object LotSize {
  lazy implicit val PriceFilterDecoder: Decoder[LotSize] = deriveDecoder
}

object MinNotational {
  lazy implicit val PriceFilterDecoder: Decoder[MinNotational] = deriveDecoder
}

object IcebergParts {
  lazy implicit val PriceFilterDecoder: Decoder[IcebergParts] = deriveDecoder
}

object MarketLotSize {
  lazy implicit val PriceFilterDecoder: Decoder[MarketLotSize] = deriveDecoder
}

object MaxNumOrders {
  lazy implicit val PriceFilterDecoder: Decoder[MaxNumOrders] = deriveDecoder
}

object MaxNumAlgoOrders {
  lazy implicit val PriceFilterDecoder: Decoder[MaxNumAlgoOrders] = deriveDecoder
}