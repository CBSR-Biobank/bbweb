/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function specimenTableActionDirective() {
    var directive = {
      restrict: 'E',
      templateUrl : '/assets/javascripts/shipmentSpecimens/directives/specimenTableAction/specimenTableAction.html',
      scope: {
        action:            '=',
        onActionSelected:  '&'
      }
    };

    return directive;
  }

  return specimenTableActionDirective;
});
