/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('collapsiblePanelComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/collapsiblePanel/collapsiblePanel.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'factory');

      this.createController = function (heading) {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<collapsible-panel heading="' + heading + '"><collapsible-panel>',
          undefined,
          'collapsiblePanel');
      };
    }));

    it('has valid scope', function() {
      var heading = this.factory.stringNext();
      this.createController(heading);
      expect(this.controller.heading).toEqual(heading);
      expect(this.controller.panelOpen).toBeTrue();
    });

    it('panel can be collapsed', function() {
      var heading = this.factory.stringNext(),
          panelState;
      this.createController(heading);
      panelState = this.controller.panelOpen;
      this.controller.panelButtonClicked();
      expect(this.controller.panelOpen).toBe(!panelState);
    });


  });

});
