package object examples {

  def log(msg: String) {
    println("[" + Thread.currentThread.getName + "] " + msg)
  }

}