package std.data.quantified

sealed trait Instance[F[_]] { fa =>
  import Instance._

  type Type
  def first: Type
  def second: F[Type]

  final def mapK[G[_]](fg: F ~> G): Instance[G] =
    Value((first, fg.apply(second)))

  final def toScala: (A, F[A]) forSome { type A } =
    (first, second)

  final def toExists: Exists[λ[X => (X, F[X])]] =
    Exists.from(this)

  override def toString: String = first.toString
}
object Instance {
  private final case class Value[F[_], A](tuple: (A, F[A])) extends Instance[F] {
    type Type = A
    val first: A = tuple._1
    val second: F[A] = tuple._2
  }

  def fromScala[F[_]](fa: (X, F[X]) forSome { type X }): Instance[F] =
    Value(fa)

  def fromExists[F[_]](exists: Exists[λ[X => (X, F[X])]]): Instance[F] =
    Value(Exists.unwrap[λ[X => (X, F[X])]](exists))

  def apply[F[_]]: PartialApply[F] = new PartialApply[F]

  private[quantified]
  final class PartialApply[F[_]] {
    def apply[A](a: A)(implicit A: F[A]): Instance[F] = Value[F, A]((a, A))
  }

  implicit def capture[F[_], A](a: A)(implicit A: F[A]): Instance[F] = new Instance[F] {
    type Type = A
    def first: A = a
    def second: F[A] = A
  }

  def unapply[F[_]](box: Instance[F]): Option[(box.Type, F[box.Type])] =
    Option(box.first -> box.second)
}