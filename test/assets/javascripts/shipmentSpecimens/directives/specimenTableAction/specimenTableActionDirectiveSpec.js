/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('specimenTableActionDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(DirectiveTestSuiteMixin, testUtils) {
      _.extend(this, DirectiveTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/shipmentSpecimens/directives/specimenTableAction/specimenTableAction.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'factory');
      testUtils.addCustomMatchers();

      this.createScope = function (action, onActionSelected) {
        DirectiveTestSuiteMixin.prototype.createScope.call(
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
    }));

    it('has valid scope', function() {
      var action = { id:    'remove',
                     class: 'btn-warning',
                     title: 'Remove specimen',
                     icon:  'glyphicon-remove'
                   },
          onActionSelected = jasmine.createSpy('onActionSelected').and.returnValue(null);
      this.createScope(action, onActionSelected);

      expect(this.scope.vm.action).toBe(action);
      expect(this.scope.vm.onActionSelected).toBeFunction();
    });

    it('can invoke callback function', function() {
      var action = {},
          onActionSelected = jasmine.createSpy('onActionSelected').and.returnValue(null);
      this.createScope(action, onActionSelected);

      this.scope.vm.onActionSelected();
      this.scope.$digest();
      expect(onActionSelected).toHaveBeenCalled();
    });


  });

});
