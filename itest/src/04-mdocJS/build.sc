import $file.plugins
import $file.SimpleModule
import mill._
import mill.scalalib._
import io.github.quafadas.millSite._
import ClasspathHelp._

object foo extends SimpleModule.SimpleModule

// Single module setup
object simpleJs extends SiteJSModule {
  override def scalaVersion = "3.3.1"
  override def scalaJSVersion = "1.14.0"
}

object withJsProject extends SiteModule {
  override def scalaVersion = "3.3.1"
  override val jsSiteModule = simpleJs
  def moduleDeps = Seq(foo)
}

def verify() = T.command {

  val mdocProps = simpleJs.mdocJsProperties()
  // println(os.read(mdocProps.path / "mdoc.properties"))
  assert(os.exists(mdocProps.path / "mdoc.properties"))
  val mdocProperties = os.read(mdocProps.path / "mdoc.properties")
  assert(mdocProperties.contains("""js-out-prefix=_assets/js"""))
  assert(mdocProperties.contains("""js-scalac-options"""))
  assert(mdocProperties.contains("""js-linker-classpath"""))
  assert(mdocProperties.contains("""js-classpath"""))
  assert(mdocProperties.contains("""js-module-kind=NoModule"""))

  val mdocOut = withJsProject.mdoc().path
  assert(os.exists(mdocOut / "_docs" / "some.mdoc.md"))
  assert(os.exists(mdocOut / "_assets" / "js" / "some.mdoc.md.js")) // subRelTo puts these in the right place.
  assert(os.exists(mdocOut / "_assets" / "js" / "mdoc.js"))

  val site = withJsProject.live()
  assert(os.exists(site / "docs" / "some.mdoc.html"))
  assert(os.read(site / "docs" / "some.mdoc.html").contains("""src="../js/mdoc.js""") )
  assert(os.exists(site / "js" / "some.mdoc.md.js"))
  assert(os.exists(site / "js" / "mdoc.js"))

  val toPublish = withJsProject.publishDocs().path
  assert(os.read(toPublish / "docs" / "some.mdoc.html").contains("""src="../js/mdoc.js""") )
  assert(os.exists(toPublish / "js" / "some.mdoc.md.js"))
  assert(os.exists(toPublish / "js" / "mdoc.js"))

}
