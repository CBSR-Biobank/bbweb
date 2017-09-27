/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

/**
 * TODO: not sure how to test open / closed state of the panel since it is a ui-bootstrap panel.
 */
describe('Directive: panelButtons', function() {
  var modelFuncNames = ['add', 'panelToggle'];

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);

      this.injectDependencies('$rootScope', '$compile');

      this.element = angular.element(
        `<panel-buttons on-add="model.add()"
           add-button-title="add location"
           add-button-enabled="model.addEnabled"
           panel-open="model.panelOpen">
         </panel-buttons>'`);

      this.createScope = (options) => {
        options = options || {};
        this.scope = this.$rootScope.$new();
        this.scope.model = {
          add:         function () {},
          addEnabled:  options.addEnabled || false,
          panelOpen:   true,
          panelToggle: function () {}
        };

        modelFuncNames.forEach((funcName) => spyOn(this.scope.model, funcName).and.returnValue(funcName));

        this.$compile(this.element)(this.scope);
        this.scope.$digest();
      };
    });
  });

  it('clicking on a button invokes corresponding function', function() {
    var buttons;

    this.createScope({ addEnabled: true });

    buttons = this.element.find('button');
    expect(buttons.length).toBe(2);
    buttons.eq(0).click();
    expect(this.scope.model.add).toHaveBeenCalled();
  });

  it('button not present if disabled', function() {
    var buttons;

    this.createScope({ addEnabled: false });
    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);
  });

  it('add button has the correct icon', function() {
    var icons;

    this.createScope({ addEnabled: true });
    icons = this.element.find('button i');
    expect(icons.length).toBe(2);
    expect(icons.eq(0)).toHaveClass('glyphicon-plus');
  });

});
