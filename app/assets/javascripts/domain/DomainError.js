/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import ExtendableError from 'es6-error';

function DomainErrorFactory() {

  /**
   * Exception used by the {@link domain} layer when an programming error is encountered.
   *
   * @memberOf domain
   *
   * @param {string} message - the error message to associate with this exception.
   */
  class DomainError extends ExtendableError {}

  return DomainError;
}

export default ngModule => ngModule.factory('DomainError', DomainErrorFactory)
