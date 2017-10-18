/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @return {object} Object containing the functions that will be mixed in.
 */
function ServerReplyMixin () {

  return {
    reply: reply,
    errorReply: errorReply
  };

  function reply(obj = {}) {
    return { status: 'success', data: obj };
  }

  function errorReply(message = 'error') {
    return { status: 'error', message: message };
  }

}


export default ngModule => ngModule.service('ServerReplyMixin', ServerReplyMixin)
