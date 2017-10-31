/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */


function BbwebErrorFactory() {

  /**
   * Description
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
