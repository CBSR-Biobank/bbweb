/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  function serverReplyMixinFactory($templateCache) {

    var mixin = {
      reply: reply,
      errorReply: errorReply
    };

    return mixin;

    //---

    function reply(obj) {
      return { status: 'success', data: obj || {} };
    }

    function errorReply(message) {
      return { status: 'error', message: message || 'error'};
    }


  }

  return serverReplyMixinFactory;

});
