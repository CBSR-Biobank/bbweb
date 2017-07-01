/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sprintf = require('sprintf-js').sprintf;

  describe('Component: statusLine', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (entity, showState) {
        showState = showState || true;

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          sprintf('<status-line item="vm.entity" show-state="%s"></status-line>', showState.toString()),
          { entity: entity },
          'statusLine');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'Centre',
                              '$filter',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/common/components/statusLine/statusLine.html');
    }));

    it('has the correct number of table cells', function() {
      var centre = new this.Centre(this.factory.centre()),
          cells;

      this.createController(centre);
      cells = this.element.find('td');
      expect(cells.length).toBe(3);
      _.each(_.range(3), function (n) {
        var small = cells.eq(n).find('small');
        expect(small.eq(0)).toHaveClass('text-muted');
      });

      expect(cells.eq(0).find('small').eq(0).text()).toContain(centre.state);
      expect(cells.eq(1).find('small').eq(0).text()).toContain(this.$filter('timeago')(centre.timeAdded));
    });

    it('displays correpct value if timeModified has a value', function() {
      var centre = new this.Centre(this.factory.centre()),
          cells;

      this.createController(centre);
      cells = this.element.find('td');

      expect(centre.timeModified).toBeDefined();
      expect(cells.eq(2).find('small').eq(0).text()).toContain(this.$filter('timeago')(centre.timeModified));
    });

    it('displays correct value if timeModified is undefined', function() {
      var centre = new this.Centre(this.factory.centre()),
          cells;

      centre.timeModified = undefined;
      this.createController(centre);

      cells = this.element.find('td');
      expect(cells.eq(2).find('small').eq(0).text()).toContain('Never');
    });

    it('does not show status if `showStatus` is false', function() {
      var centre = new this.Centre(this.factory.centre()),
          cells;

      this.createController(centre, false);
      cells = this.element.find('td');
      expect(cells.eq(0).find('small').eq(0).text()).not.toContain(centre.statusLabel);
    });


  });

});
