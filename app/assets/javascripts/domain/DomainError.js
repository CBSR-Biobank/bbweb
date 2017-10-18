/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

function DomainErrorFactory() {

  /**
   * Description
   */
  function DomainError(message) {
    this.message = message;
    this.stack = (new Error()).stack;
  }

  DomainError.prototype = Object.create(Error.prototype);
  DomainError.prototype.constructor = DomainError;
  DomainError.prototype.name = 'DomainError';

  return DomainError;
}

export default ngModule => ngModule.factory('DomainError', DomainErrorFactory)
