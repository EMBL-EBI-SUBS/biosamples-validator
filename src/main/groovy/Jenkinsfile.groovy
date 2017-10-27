
node {
    stage('Preparation') { // for display purposes
        // Get some code from a GitHub repository
        checkout scm
        //gradleHome = tool 'gradle-3'
    }
    stage('Build') {
        // Run the gradle assemble
        echo 'Building'
        sh "./gradlew assemble"
    }
    stage('Deploy') {
       sh 'echo branch $BRANCH_NAME'
        sh 'git name-rev --name-only HEAD > GIT_BRANCH'
        sh 'cat GIT_BRANCH'
        git_branch = readFile('GIT_BRANCH').trim()
        echo "git_branch $git_branch"
         //Run the gradle assemble
        //sh "gradlew -Penv=$BRANCH_NAME"
    }
}