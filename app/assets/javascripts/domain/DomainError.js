/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function DomainErrorFactory($log) {

  /**
   * @class
   * Exception used by the {@link domain} layer when an programming error is encountered.
   *
   * @memberOf domain
   *
   * @param {string} message - the error message to associate with this exception.
   */
  function DomainError(message) {
    this.message = message;
    this.stack = (new Error()).stack;
    $log.error('DomainError', message);
  }

  DomainError.prototype = Object.create(Error.prototype);
  DomainError.prototype.constructor = DomainError;
  DomainError.prototype.name = 'DomainError';

  return DomainError;
}

export default ngModule => ngModule.factory('DomainError', DomainErrorFactory)
