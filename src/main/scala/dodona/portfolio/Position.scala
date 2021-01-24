package dodona.portfolio

case class Position(
    action: String,
    status: String,
    price: BigDecimal,
    quantity: BigDecimal,
    transactionTime: Long
)
