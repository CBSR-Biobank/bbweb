/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function biobankFooterDirective() {
    var directive = {
      restrict: 'E',
      controller: BiobankFooterCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  BiobankFooterCtrl.$inject = [];

  function BiobankFooterCtrl() {

  }

  return biobankFooterDirective;

});
