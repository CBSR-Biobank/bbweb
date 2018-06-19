/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { DirectiveTestSuiteMixin } from 'test/mixins/DirectiveTestSuiteMixin';
import ngModule from '../../../app';

/**
 * TODO: not sure how to test open / closed state of the panel since it is a ui-bootstrap panel.
 */
describe('Component: panelButtons', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, DirectiveTestSuiteMixin);

      this.injectDependencies('$rootScope', '$compile');

      this.element = angular.element();

      this.createController = (options = {}) => {
        this.createControllerInternal(
          `<panel-buttons
              on-add="vm.add()"
              add-button-title="add location"
              add-button-enabled="vm.addEnabled"
              panel-open="model.panelOpen">
           </panel-buttons>'`,
          {
            add:         jasmine.createSpy().and.returnValue(null),
            addEnabled:  options.addEnabled || false,
            panelOpen:   true,
            panelToggle: jasmine.createSpy().and.returnValue(null)
          });
      };
    });
  });

  it('clicking on a button invokes corresponding function', function() {
    this.createController({ addEnabled: true });
    const buttons = this.element.find('button');
    expect(buttons.length).toBe(2);
    buttons.eq(0).click();
    expect(this.controller.add).toHaveBeenCalled();
  });

  it('button not present if disabled', function() {
    this.createController({ addEnabled: false });
    const buttons = this.element.find('button');
    expect(buttons.length).toBe(1);
  });

  it('add button has the correct icon', function() {
    this.createController({ addEnabled: true });
    const icons = this.element.find('button i');
    expect(icons.length).toBe(2);
    expect(icons.eq(0)).toHaveClass('glyphicon-plus');
  });

});
