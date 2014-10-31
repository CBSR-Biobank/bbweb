module.exports = function(grunt) {

  //	Initialize the grunt tasks
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    karma: {
      unit: {
        configFile: 'karma.conf.js'
      }
    },

    ngAnnotate: {
      options: {
        add: false,
        remove: false,
        singleQuotes: true
      },
      bbwebApp: {
        files: [
          {
            expand: true,
            src: ['app/assets/javascripts/**/*.js'],
            ext: '.annotated.js', // Dest filepaths will have this extension.
            extDot: 'last'       // Extensions in filenames begin after the last dot
          },
        ],
      }
    },

    protractor: {
      options: {
        configFile: "node_modules/protractor/referenceConf.js", // Default config file
        keepAlive: true, // If false, the grunt process stops when the test fails.
        noColor: false, // If true, protractor will not use colors in its output.
        args: {
          // Arguments passed to the command
        }
      },
      your_target: {   // Grunt requires at least one target to run so you can simply put 'all: {}' here too.
        options: {
          configFile: "e2e.conf.js", // Target-specific config file
          args: {} // Target-specific arguments
        }
      },
    }

  });

  grunt.loadNpmTasks('grunt-karma');
  grunt.registerTask('default', ['karma']);
  grunt.loadNpmTasks('grunt-ng-annotate');
};
