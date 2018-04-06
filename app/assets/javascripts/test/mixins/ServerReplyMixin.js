/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @exports test.mixins.ServerReplyMixin
 */
const ServerReplyMixin = {

  /**
   * Returns a plain object that simulates a reply from the server.
   *
   * @param {object} obj - the object to return in the `data` property.
   */
  reply: function (obj = {}) {
    return { status: 'success', data: obj };
  },

  /**
   * Returns a plain object that simulates an error reply from the server.
   *
   * @param {string} message - the error message to return.
   */
  errorReply: function (message = 'error') {
    return { status: 'error', message: message };
  }

}

export { ServerReplyMixin };
export default () => {};
