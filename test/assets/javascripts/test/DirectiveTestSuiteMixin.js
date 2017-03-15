/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  DirectiveTestSuiteMixinFactory.$inject = ['ComponentTestSuiteMixin'];

  function DirectiveTestSuiteMixinFactory(ComponentTestSuiteMixin) {

    function DirectiveTestSuiteMixin() {
      ComponentTestSuiteMixin.call(this);
    }

    DirectiveTestSuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
    DirectiveTestSuiteMixin.prototype.constructor = DirectiveTestSuiteMixin;

    return DirectiveTestSuiteMixin;
  }

  return DirectiveTestSuiteMixinFactory;

});
