package domain

package service {

  /**
   * All Domain Services extend this trait.
   *
   * @author Nelson Loyola
   */
  trait DomainService {

    /**
     * A partial function to handle each command. The input is a Tuple3 consisting of:
     *
     *  1. The command to handle.
     *  2. The study entity the command is associated with,
     *  3. The event message listener to be notified if the command is successful.
     *
     *  If the command is invalid, then the method throws an Error exception.
     */
    def process

  }

}