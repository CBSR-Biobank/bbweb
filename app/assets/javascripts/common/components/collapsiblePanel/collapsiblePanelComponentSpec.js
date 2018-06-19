/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../../app';

describe('collapsiblePanelComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Factory');

      this.createController = (heading) =>
        this.createControllerInternal(
          '<collapsible-panel heading="' + heading + '"><collapsible-panel>',
          undefined,
          'collapsiblePanel');
    });
  });

  it('has valid scope', function() {
    var heading = this.Factory.stringNext();
    this.createController(heading);
    expect(this.controller.heading).toEqual(heading);
    expect(this.controller.panelOpen).toBeTrue();
  });

  it('panel can be collapsed', function() {
    var heading = this.Factory.stringNext(),
        panelState;
    this.createController(heading);
    panelState = this.controller.panelOpen;
    this.controller.panelButtonClicked();
    expect(this.controller.panelOpen).toBe(!panelState);
  });

});
