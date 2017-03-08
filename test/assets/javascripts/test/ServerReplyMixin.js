/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');


  function ServerReplyMixinFactory($templateCache) {

    function ServerReplyMixin() {}

    ServerReplyMixin.prototype.reply = function (obj) {
      obj = _.isUndefined(obj) ? {} : obj;
      return { status: 'success', data: obj };
    };

    ServerReplyMixin.prototype.errorReply = function(message) {
       return { status: 'error', message: message || 'error'};
    };

    return ServerReplyMixin;
  }

  return ServerReplyMixinFactory;

});
