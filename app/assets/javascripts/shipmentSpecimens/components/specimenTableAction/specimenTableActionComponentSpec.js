/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: specimenTableAction', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (action, onActionSelected) {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<specimen-table-action ' +
            ' action="action"' +
            ' on-action-selected="vm.onActionSelected">' +
            '</specimen-table-action>',
          {
            action:           action,
            onActionSelected: onActionSelected
          },
          'specimenTableAction');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin, testUtils) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.putHtmlTemplates(
        '/assets/javascripts/shipmentSpecimens/components/specimenTableAction/specimenTableAction.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'factory');
      testUtils.addCustomMatchers();

    }));

    it('has valid scope', function() {
      var action = { id:    'remove',
                     class: 'btn-warning',
                     title: 'Remove specimen',
                     icon:  'glyphicon-remove'
                   },
          onActionSelected = jasmine.createSpy('onActionSelected').and.returnValue(null);
      this.createController(action, onActionSelected);

      expect(this.scope.vm.action).toBe(action);
      expect(this.scope.vm.onActionSelected).toBeFunction();
    });

    it('can invoke callback function', function() {
      var action = {},
          onActionSelected = jasmine.createSpy('onActionSelected').and.returnValue(null);
      this.createController(action, onActionSelected);

      this.scope.vm.onActionSelected();
      this.scope.$digest();
      expect(onActionSelected).toHaveBeenCalled();
    });


  });

});
