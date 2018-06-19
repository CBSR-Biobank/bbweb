/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import ExtendableError from 'es6-error';

function BbwebErrorFactory() {

  /**
   * Exception used by the Biobank {@link AngularJS} layer when an programming error is encountered.
   *
   * @memberOf base
   *
   * @param {string} message - the error message to associate with this exception.
   */
  class BbwebError extends ExtendableError {}

  return BbwebError;
}

export default ngModule => ngModule.factory('BbwebError', BbwebErrorFactory)
