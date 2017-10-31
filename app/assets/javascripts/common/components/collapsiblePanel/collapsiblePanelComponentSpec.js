/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('collapsiblePanelComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Factory');

      this.createController = (heading) =>
        ComponentTestSuiteMixin.createController.call(
          this,
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
