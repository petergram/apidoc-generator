package generator

sealed trait ScalaPrimitive {
  def name: String
  def asString(originalVarName: String): String
}

object ScalaPrimitive {

  case object Boolean extends ScalaPrimitive {
    def name = "Boolean"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName.toString"
    }
  }

  case object Double extends ScalaPrimitive {
    def name = "Double"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName.toString"
    }
  }

  case object Integer extends ScalaPrimitive {
    def name = "Int"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName.toString"
    }
  }

  case object Long extends ScalaPrimitive {
    def name = "Long"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName.toString"
    }
  }

  case object DateIso8601 extends ScalaPrimitive {
    def name = "_root_.org.joda.time.LocalDate"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName.toString"
    }
  }

  case object DateTimeIso8601 extends ScalaPrimitive {
    def name = "_root_.org.joda.time.DateTime"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"_root_.org.joda.time.format.ISODateTimeFormat.dateTime.print($varName)"
    }
  }

  case object Decimal extends ScalaPrimitive {
    def name = "Decimal"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName.toString"
    }
  }

  case object Object extends ScalaPrimitive {
    def name = "_root_.play.api.libs.json.JsObject"
    def asString(originalVarName: String): String = {
      throw new UnsupportedOperationException(s"unsupported conversion of type object for $originalVarName")
    }
  }

  case object String extends ScalaPrimitive {
    def name = "String"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName"
    }
  }

  case object Unit extends ScalaPrimitive {
    def name = "Unit"
    def asString(originalVarName: String): String = {
      throw new UnsupportedOperationException(s"unsupported conversion of type object for $originalVarName")
    }
  }

  case object Uuid extends ScalaPrimitive {
    def name = "_root_.java.util.UUID"
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName.toString"
    }
  }

  case class Model(name: String) extends ScalaPrimitive {
    def asString(originalVarName: String): String = {
      throw new UnsupportedOperationException(s"unsupported conversion of type model for $originalVarName")
    }
  }

  case class Enum(name: String) extends ScalaPrimitive {
    def asString(originalVarName: String): String = {
      val varName = ScalaUtil.quoteNameIfKeyword(originalVarName)
      s"$varName.toString"
    }
  }

}

sealed trait ScalaDatatype {
  def nilValue: Option[String]

  // TODO: UNION TYPES - change to names: Seq[String] or similar
  def name: String
}

object ScalaDatatype {
  case class List(types: Seq[ScalaPrimitive]) extends ScalaDatatype {
    override def nilValue = Some("Nil")
    override def name = types match {
      case single :: Nil => s"Seq[${single.name}]"
      case multiple => sys.error("TODO: UNION TYPES")
    }
  }

  case class Map(types: Seq[ScalaPrimitive]) extends ScalaDatatype {
    override def nilValue = Some("Map.empty")
    override def name = types match {
      case single :: Nil => s"Map[${single.name}]"
      case multiple => sys.error("TODO: UNION TYPES")
    }
  }

  case class Option(types: Seq[ScalaPrimitive]) extends ScalaDatatype {
    override def nilValue = Some("None")
    override def name = types match {
      case single :: Nil => s"_root_.scala.Option[${single.name}]"
      case multiple => sys.error("TODO: UNION TYPES")
    }
  }

  case class Singleton(types: Seq[ScalaPrimitive]) extends ScalaDatatype {
    override def nilValue = None
    override def name = types match {
      case single :: Nil => single.name
      case multiple => sys.error("TODO: UNION TYPES")
    }
  }

}

case class ScalaTypeResolver(
  modelPackageName: String,
  enumPackageName: String
) {

  def scalaPrimitive(t: Type): ScalaPrimitive = {
    t match {
      case Type(TypeKind.Primitive, name) => {
        Primitives(name) match {
          case Boolean => ScalaPrimitive.Boolean
          case Decimal => ScalaPrimitive.Decimal
          case Integer => ScalaPrimitive.Integer
          case Double => ScalaPrimitive.Double
          case Long => ScalaPrimitive.Long
          case Object => ScalaPrimitive.Object
          case String => ScalaPrimitive.String
          case DateIso8601 => ScalaPrimitive.DateIso8601
          case DateTimeIso8601 => ScalaPrimitive.DateTimeIso8601
          case Uuid => ScalaPrimitive.Uuid
          case Unit => ScalaPrimitive.Unit
        }
      }
      case Type(TypeKind.Model, name) => {
        ScalaPrimitive.Model(s"${modelPackageName}.${ScalaUtil.toClassName(name)}")
      }
      case Type(TypeKind.Enum, name) => {
        ScalaPrimitive.Enum(s"${enumPackageName}.${ScalaUtil.toClassName(name)}")
      }
    }
  }

  def scalaDatatype(datatype: Datatype): ScalaDatatype = {
    datatype match {
      case Datatype.List(types) => ScalaDatatype.List(types.map(scalaPrimitive(_)))

      case Datatype.Map(types) => ScalaDatatype.Map(types.map(scalaPrimitive(_)))

      case Datatype.Option(types) => ScalaDatatype.Option(types.map(scalaPrimitive(_)))

      case Datatype.Singleton(types) => types.map(scalaPrimitive(_)) match {
        case single :: Nil => single
        case multiple => sys.error("TODO: UNION TYPE")
      }
    }
  }

}
