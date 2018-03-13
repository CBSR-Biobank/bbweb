/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */


function BbwebErrorFactory() {

  /**
   * @class
   * Exception used by the Biobank {@link AngularJS} layer when an programming error is encountered.
   *
   * @memberOf base
   *
   * @param {string} message - the error message to associate with this exception.
   */
  function BbwebError(message) {
    this.message = message;
    this.stack = (new Error()).stack;
  }

  BbwebError.prototype = Object.create(Error.prototype);
  BbwebError.prototype.constructor = BbwebError;
  BbwebError.prototype.name = 'BbwebError';

  return BbwebError;
}

export default ngModule => ngModule.factory('BbwebError', BbwebErrorFactory)
